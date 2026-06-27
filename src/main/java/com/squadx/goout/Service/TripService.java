package com.squadx.goout.Service;

import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.TripRepository;
import com.squadx.goout.Repository.UserRepository;
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

    // 1. User asks to join (Goes to waiting room)
    public void requestToJoinTrip(String tripId, String userEmail) {

        // Fix: Actually fetch the user and the trip from the database first!
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Security: Don't let the organizer join their own trip
        if (trip.getOrganizerId().equals(user.getId())) {
            throw new RuntimeException("You are the organizer of this trip!");
        }

        // Security: Don't let them request if they are already in the trip or already pending
        if (trip.getParticipantIds().contains(user.getId())) {
            throw new RuntimeException("You are already a participant.");
        }
        if (trip.getPendingJoinRequests().contains(user.getId())) {
            throw new RuntimeException("Your request is already pending approval.");
        }

        // Add to waiting room
        trip.getPendingJoinRequests().add(user.getId());
        tripRepository.save(trip);
    }

    // 2. Admin views the waiting room
    public List<UserSummaryDto> getPendingRequests(String tripId, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getOrganizerId().equals(admin.getId())) {
            throw new AccessDeniedException("Only the trip organizer can view requests.");
        }

        // Loop through the pending IDs, look up the User in MongoDB, and convert them to a DTO!
        return trip.getPendingJoinRequests().stream()
                .map(userId -> userRepository.findById(userId)
                        .map(u -> new UserSummaryDto(u.getId(), u.getFirstName() + " " + u.getLastName(), u.getEmail()))
                        .orElse(new UserSummaryDto(userId, "Unknown User", "Unknown Email")))
                .collect(Collectors.toList());
    }

    // 3. Admin accepts or rejects a user
    public void resolveJoinRequest(String tripId, String requesterId, String status, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Security check: Only organizer can approve
        if (!trip.getOrganizerId().equals(admin.getId())) {
            throw new AccessDeniedException("Only the trip organizer can approve requests.");
        }

        // Remove them from the waiting room regardless of yes/no
        boolean wasInWaitingRoom = trip.getPendingJoinRequests().remove(requesterId);

        if (!wasInWaitingRoom) {
            throw new RuntimeException("This user is not in the pending requests list.");
        }

        // If ACCEPTED, move them to the official participants list
        if ("ACCEPTED".equalsIgnoreCase(status)) {
            trip.getParticipantIds().add(requesterId);
        }

        tripRepository.save(trip);
    }
}