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

    public List<TripResponseDto> getGlobalUpcomingFeed(String currentUserEmail, String search) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String currentUserId = currentUser.getId();

        List<Trip> allPublicTrips = tripRepository.findByIsPublicTrue();

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

            User org = userRepository.findById(trip.getOrganizerId()).orElse(null);
            TripResponseDto.TripMemberDto organizerDto = null;

            if (org != null) {
                organizerDto = new TripResponseDto.TripMemberDto(
                        org.getId(), org.getFirstName(), org.getLastName(), getSmartAvatar(org)
                );
            }

            int likeCount = (trip.getLikedBy() != null) ? trip.getLikedBy().size() : 0;
            boolean isLiked = (trip.getLikedBy() != null) && trip.getLikedBy().contains(currentUserId);

            TripResponseDto dto = new TripResponseDto(
                    trip.getId(), trip.getTitle(), trip.getDescription(), trip.getDestinations(),
                    trip.getImageUrl(), trip.getStartDate(), trip.getEndDate(), trip.getMinBudget(),
                    trip.getMaxBudget(), trip.getMaxParticipants(), trip.getOrganizerId(),
                    trip.getStatus(),
                    trip.getGalleryImages(), // <-- 🌟 CRITICAL FIX: Added this to satisfy the DTO Constructor!
                    likeCount,
                    isLiked,
                    "NONE",
                    organizerDto,
                    new ArrayList<>()
            );

            feed.add(dto);
        }

        return feed;
    }

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

        trip.setStatus("COMPLETED");
        return tripRepository.save(trip);
    }

    public void requestToJoinTrip(String tripId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // 🌟 NEW SECURITY FIX: Block requests if the trip is already full!
        int maxCapacity = (trip.getMaxParticipants() != null) ? trip.getMaxParticipants() : 0;
        int currentMembers = (trip.getParticipantIds() != null) ? trip.getParticipantIds().size() : 0;

        if (maxCapacity > 0 && currentMembers >= maxCapacity) {
            throw new RuntimeException("Cannot join: This trip has already reached its maximum capacity of " + maxCapacity + " travelers.");
        }

        if (trip.getOrganizerId().equals(user.getId())) {
            throw new RuntimeException("You are the organizer of this trip!");
        }
        if (trip.getParticipantIds() != null && trip.getParticipantIds().contains(user.getId())) {
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

            // 🌟 NEW SECURITY FIX: Block the organizer from accepting if the trip is full!
            int maxCapacity = (trip.getMaxParticipants() != null) ? trip.getMaxParticipants() : 0;
            if (maxCapacity > 0 && trip.getParticipantIds().size() >= maxCapacity) {
                // If they try to accept, we throw an error and put the user BACK into the waiting room so their request isn't lost!
                trip.getPendingJoinRequests().add(requesterId);
                throw new RuntimeException("Cannot accept user: The trip has already reached its maximum capacity of " + maxCapacity + " members.");
            }

            trip.getParticipantIds().add(requesterId);
        }

        tripRepository.save(trip);
    }

    public void toggleLike(String tripId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getLikedBy() == null) {
            trip.setLikedBy(new ArrayList<>());
        }

        if (trip.getLikedBy().contains(currentUser.getId())) {
            trip.getLikedBy().remove(currentUser.getId());
        } else {
            trip.getLikedBy().add(currentUser.getId());
        }

        tripRepository.save(trip);
    }

    public void deleteTrip(String tripId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getOrganizerId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only the trip organizer can delete this trip!");
        }

        tripRepository.delete(trip);
    }

    // 🌟 NEW: The Edit Trip Logic with Security Check
    public Trip updateTrip(String tripId, Trip updatedTripData, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // SECURITY: Only the organizer can edit the trip!
        if (!trip.getOrganizerId().equals(user.getId())) {
            throw new AccessDeniedException("Only the trip organizer can edit these trip details!");
        }

        // Update the allowed fields if the frontend sent them
        if (updatedTripData.getTitle() != null) trip.setTitle(updatedTripData.getTitle());
        if (updatedTripData.getDestinations() != null) trip.setDestinations(updatedTripData.getDestinations());
        if (updatedTripData.getStartDate() != null) trip.setStartDate(updatedTripData.getStartDate());
        if (updatedTripData.getEndDate() != null) trip.setEndDate(updatedTripData.getEndDate());
        if (updatedTripData.getMinBudget() != null) trip.setMinBudget(updatedTripData.getMinBudget());
        if (updatedTripData.getMaxBudget() != null) trip.setMaxBudget(updatedTripData.getMaxBudget());
        if (updatedTripData.getMaxParticipants() != null) trip.setMaxParticipants(updatedTripData.getMaxParticipants());
        if (updatedTripData.getDescription() != null) trip.setDescription(updatedTripData.getDescription());

        return tripRepository.save(trip);
    }
}