package com.squadx.goout.Service;

import com.squadx.goout.Dto.TripResponseDto;
import com.squadx.goout.Dto.UserSummaryDto;
import com.squadx.goout.Entity.Post;
import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.PostRepository;
import com.squadx.goout.Repository.TripRepository;
import com.squadx.goout.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 🌟 NEW: Fetches global trips and attaches the Organizer's name & photo!
    public List<TripResponseDto> getGlobalUpcomingFeed() {
        List<Trip> upcomingTrips = tripRepository.findByIsPublicTrueAndStatus("UPCOMING");
        List<TripResponseDto> feed = new ArrayList<>();

        for (Trip trip : upcomingTrips) {

            // 1. Look up the person who created this trip
            User org = userRepository.findById(trip.getOrganizerId()).orElse(null);
            TripResponseDto.TripMemberDto organizerDto = null;

            if (org != null) {

                // 🌟 THE FIX: Smart Avatar Fallback Logic
                String avatar = org.getAvatarUrl();

                // Fallback 1: Check the older profile image field just in case
                if (avatar == null || avatar.trim().isEmpty()) {
                    avatar = org.getProfileImageUrl();
                }

                // Fallback 2: Generate a dynamic initial avatar if they have no photo!
                if (avatar == null || avatar.trim().isEmpty()) {
                    String fName = org.getFirstName() != null ? org.getFirstName() : "Go";
                    String lName = org.getLastName() != null ? org.getLastName() : "Out";
                    // Creates a nice blue circle with their white initials
                    avatar = "https://ui-avatars.com/api/?name=" + fName + "+" + lName + "&background=0EA5E9&color=fff&rounded=true";
                }

                organizerDto = new TripResponseDto.TripMemberDto(
                        org.getId(), org.getFirstName(), org.getLastName(), avatar // <-- Using the smart avatar!
                );
            }

            // 2. Package it all up safely for the frontend
            TripResponseDto dto = new TripResponseDto(
                    trip.getId(), trip.getTitle(), trip.getDescription(), trip.getDestinations(),
                    trip.getImageUrl(), trip.getStartDate(), trip.getEndDate(), trip.getMinBudget(),
                    trip.getMaxBudget(), trip.getMaxParticipants(), trip.getOrganizerId(),
                    trip.getStatus(),
                    organizerDto, // <-- Attached the organizer details here!
                    new ArrayList<>() // Empty joined members to save bandwidth on the main feed
            );

            feed.add(dto);
        }

        return feed;
    }

    // ==========================================
    // EXISTING ENTERPRISE LOGIC
    // ==========================================
    public Trip completeTrip(String tripId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getOrganizerId().equals(user.getId())) {
            throw new AccessDeniedException("Only the trip organizer can end this trip!");
        }

        trip.setStatus("COMPLETED");
        Trip savedTrip = tripRepository.save(trip);

        Post memoryPost = new Post();
        memoryPost.setAuthorId(user.getId());
        memoryPost.setContent("Just completed an amazing trip: " + trip.getTitle() + "! 🌍✈️");
        memoryPost.setLocation(trip.getDestinations());
        memoryPost.setCreatedAt(java.time.LocalDateTime.now());

        if (trip.getImageUrl() != null && !trip.getImageUrl().isEmpty()) {
            memoryPost.setImageUrl(trip.getImageUrl());
        }

        postRepository.save(memoryPost);
        return savedTrip;
    }

    public void requestToJoinTrip(String tripId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getOrganizerId().equals(user.getId())) {
            throw new RuntimeException("You are the organizer of this trip!");
        }
        if (trip.getParticipantIds().contains(user.getId())) {
            throw new RuntimeException("You are already a participant.");
        }
        if (trip.getPendingJoinRequests().contains(user.getId())) {
            throw new RuntimeException("Your request is already pending approval.");
        }

        trip.getPendingJoinRequests().add(user.getId());
        tripRepository.save(trip);
    }

    public List<UserSummaryDto> getPendingRequests(String tripId, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getOrganizerId().equals(admin.getId())) {
            throw new AccessDeniedException("Only the trip organizer can view requests.");
        }

        return trip.getPendingJoinRequests().stream()
                .map(userId -> userRepository.findById(userId)
                        .map(u -> {
                            String fName = u.getFirstName() != null ? u.getFirstName() : "";
                            String lName = u.getLastName() != null ? u.getLastName() : "";
                            String fullName = (fName + " " + lName).trim();
                            if (fullName.isEmpty()) fullName = "Traveler";
                            return new UserSummaryDto(u.getId(), fullName, u.getEmail());
                        })
                        .orElse(new UserSummaryDto(userId, "Unknown User", "Unknown Email")))
                .collect(Collectors.toList());
    }

    public void resolveJoinRequest(String tripId, String requesterId, String status, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getOrganizerId().equals(admin.getId())) {
            throw new AccessDeniedException("Only the trip organizer can approve requests.");
        }

        boolean wasInWaitingRoom = trip.getPendingJoinRequests().remove(requesterId);

        if (!wasInWaitingRoom) {
            throw new RuntimeException("This user is not in the pending requests list.");
        }

        if ("ACCEPTED".equalsIgnoreCase(status)) {
            trip.getParticipantIds().add(requesterId);
        }

        tripRepository.save(trip);
    }
}