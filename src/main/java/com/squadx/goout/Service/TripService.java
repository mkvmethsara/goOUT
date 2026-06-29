package com.squadx.goout.Service;

import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Entity.Post;
import com.squadx.goout.Repository.TripRepository;
import com.squadx.goout.Repository.UserRepository;
import com.squadx.goout.Repository.PostRepository;
import com.squadx.goout.Dto.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository; // <-- Moved here! The proper place for business logic.

    // ==========================================
    // 🌟 NEW: The Enterprise-Grade "End Trip" Logic
    // ==========================================
    public Trip completeTrip(String tripId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Security check
        if (!trip.getOrganizerId().equals(user.getId())) {
            throw new AccessDeniedException("Only the trip organizer can end this trip!");
        }

        // 1. Update Trip Status
        trip.setStatus("COMPLETED");
        Trip savedTrip = tripRepository.save(trip);

        // 2. Automated Social Post Logic (Cross-Domain Side Effect)
        Post memoryPost = new Post();
        memoryPost.setAuthorId(user.getId());
        memoryPost.setContent("Just completed an amazing trip to " + trip.getDestinations() + "! What an unforgettable adventure. 🌍✈️");

        if (trip.getImageUrl() != null && !trip.getImageUrl().isEmpty()) {
            memoryPost.setImageUrl(trip.getImageUrl());
        }

        postRepository.save(memoryPost);

        return savedTrip;
    }

    // ==========================================
    // EXISTING WAITING ROOM LOGIC
    // ==========================================

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

                            if (fullName.isEmpty()) {
                                fullName = "Traveler";
                            }

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