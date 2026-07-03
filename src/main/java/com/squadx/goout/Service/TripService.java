package com.squadx.goout.Service;

import com.squadx.goout.Dto.TripResponseDto;
import com.squadx.goout.Dto.UserSummaryDto;
import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Entity.User;
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
    // 🗑️ PostRepository removed since we use Trips as the main feed now!

    // 🌟 Fetches global trips with an optional Search Filter!
    public List<TripResponseDto> getGlobalUpcomingFeed(String currentUserEmail, String search) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String currentUserId = currentUser.getId();

        // Fetch ALL public trips (Both UPCOMING and COMPLETED)
        List<Trip> allPublicTrips = tripRepository.findByIsPublicTrue();

        // 🌟 THE FIX: If the frontend sent a search word, filter the list!
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            allPublicTrips = allPublicTrips.stream()
                    .filter(trip ->
                            (trip.getDestinations() != null && trip.getDestinations().toLowerCase().contains(searchLower)) ||
                                    (trip.getTitle() != null && trip.getTitle().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
        }

        List<TripResponseDto> feed = new ArrayList<>();

        for (Trip trip : allPublicTrips) {

            // 1. Look up the person who created this trip
            User org = userRepository.findById(trip.getOrganizerId()).orElse(null);
            TripResponseDto.TripMemberDto organizerDto = null;

            if (org != null) {
                organizerDto = new TripResponseDto.TripMemberDto(
                        org.getId(), org.getFirstName(), org.getLastName(), getSmartAvatar(org)
                );
            }

            // 🌟 Calculate the Like metrics!
            int likeCount = (trip.getLikedBy() != null) ? trip.getLikedBy().size() : 0;
            boolean isLiked = (trip.getLikedBy() != null) && trip.getLikedBy().contains(currentUserId);

            // 2. Package it all up safely for the frontend
            TripResponseDto dto = new TripResponseDto(
                    trip.getId(), trip.getTitle(), trip.getDescription(), trip.getDestinations(),
                    trip.getImageUrl(), trip.getStartDate(), trip.getEndDate(), trip.getMinBudget(),
                    trip.getMaxBudget(), trip.getMaxParticipants(), trip.getOrganizerId(),
                    trip.getStatus(),
                    likeCount, // <-- Included here!
                    isLiked,   // <-- Included here!
                    organizerDto,
                    new ArrayList<>()
            );

            feed.add(dto);
        }

        return feed;
    }

    // 🌟 Helper Method: Keeps the main method clean (Fixes IntelliJ Warning)
    private String getSmartAvatar(User org) {
        String avatar = org.getAvatarUrl();
        if (avatar == null || avatar.trim().isEmpty()) {
            avatar = org.getProfileImageUrl();
        }
        if (avatar == null || avatar.trim().isEmpty()) {
            String fName = org.getFirstName() != null ? org.getFirstName() : "Go";
            String lName = org.getLastName() != null ? org.getLastName() : "Out";
            avatar = "https://ui-avatars.com/api/?name=" + fName + "+" + lName + "&background=0EA5E9&color=fff&rounded=true";
        }
        return avatar;
    }

    public Trip completeTrip(String tripId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getOrganizerId().equals(user.getId())) {
            throw new AccessDeniedException("Only the trip organizer can end this trip!");
        }

        // Just update the status and save.
        // 🗑️ Memory Post generation logic completely removed to prevent duplicates!
        trip.setStatus("COMPLETED");
        return tripRepository.save(trip);
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

        if (trip.getPendingJoinRequests() == null) {
            trip.setPendingJoinRequests(new ArrayList<>());
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

        if (trip.getPendingJoinRequests() == null) {
            return new ArrayList<>();
        }

        return trip.getPendingJoinRequests().stream()
                .map(userId -> userRepository.findById(userId)
                        .map(u -> {
                            String fName = u.getFirstName() != null ? u.getFirstName() : "";
                            String lName = u.getLastName() != null ? u.getLastName() : "";
                            // 🌟 Fixed to perfectly match the 7 fields in UserSummaryDto
                            return new UserSummaryDto(u.getId(), fName, lName, u.getEmail(), u.getBio(), u.getLocation(), u.getAvatarUrl());
                        })
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
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

        if (trip.getPendingJoinRequests() == null) {
            throw new RuntimeException("No pending requests found.");
        }

        boolean wasInWaitingRoom = trip.getPendingJoinRequests().remove(requesterId);

        if (!wasInWaitingRoom) {
            throw new RuntimeException("This user is not in the pending requests list.");
        }

        if ("ACCEPTED".equalsIgnoreCase(status)) {
            if (trip.getParticipantIds() == null) {
                trip.setParticipantIds(new ArrayList<>());
            }
            trip.getParticipantIds().add(requesterId);
        }

        tripRepository.save(trip);
    }

    // 🌟 ADDED: The logic to Like or Unlike a trip
    public void toggleLike(String tripId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Failsafe: Ensure the list exists
        if (trip.getLikedBy() == null) {
            trip.setLikedBy(new ArrayList<>());
        }

        // The Toggle Logic: If they already liked it, remove them. Otherwise, add them.
        if (trip.getLikedBy().contains(currentUser.getId())) {
            trip.getLikedBy().remove(currentUser.getId());
        } else {
            trip.getLikedBy().add(currentUser.getId());
        }

        tripRepository.save(trip);
    }

    // 🌟 ADDED: Delete Trip Logic
    public void deleteTrip(String tripId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Security check: Only the organizer can delete it!
        if (!trip.getOrganizerId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only the trip organizer can delete this trip!");
        }

        tripRepository.delete(trip);
    }
}