package com.squadx.goout.Controller;

import com.squadx.goout.Dto.TripResponseDto;
import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.TripRepository;
import com.squadx.goout.Repository.UserRepository;
import com.squadx.goout.Service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripService tripService;

    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip, Authentication authentication) {
        String userEmail = authentication.getName();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // FIXED: Using organizerId instead of Hashen's broken ownerId
        trip.setOrganizerId(user.getId());

        Trip savedTrip = tripRepository.save(trip);
        return ResponseEntity.ok(savedTrip);
    }

    // NEW: Endpoint for the Organizer to end a trip
    @PatchMapping("/{tripId}/complete")
    public ResponseEntity<Trip> completeTrip(@PathVariable String tripId, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // SECURITY: Only the organizer can end the trip
        if (!trip.getOrganizerId().equals(user.getId())) {
            throw new RuntimeException("Only the trip organizer can end this trip!");
        }

        trip.setStatus("COMPLETED");
        Trip savedTrip = tripRepository.save(trip);

        return ResponseEntity.ok(savedTrip);
    }

    // RESTORED: Your Trip Details logic that got overwritten!
    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDto> getTripDetails(@PathVariable String id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        List<TripResponseDto.TripMemberDto> populatedMembers = new ArrayList<>();
        for (String userId : trip.getParticipantIds()) {
            userRepository.findById(userId).ifPresent(user -> {
                populatedMembers.add(new TripResponseDto.TripMemberDto(
                        user.getId(), user.getFirstName(), user.getLastName(), user.getAvatarUrl()
                ));
            });
        }

        TripResponseDto responseDto = new TripResponseDto(
                trip.getId(), trip.getTitle(), trip.getDescription(), trip.getDestinations(),
                trip.getImageUrl(), trip.getStartDate(), trip.getEndDate(), trip.getMinBudget(),
                trip.getMaxBudget(), trip.getMaxParticipants(), trip.getOrganizerId(),
                trip.getStatus(), populatedMembers
        );
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Trip>> getPublicTrips() {
        List<Trip> publicTrips = tripRepository.findByIsPublicTrue();
        return ResponseEntity.ok(publicTrips);
    }

    @GetMapping("/my-trips")
    public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String userId = user.getId();

        List<Trip> organizedTrips = tripRepository.findByOrganizerId(userId);
        organizedTrips.forEach(trip -> trip.setIsOrganizer(true)); // <-- Added "Is" here

        List<Trip> joinedTrips = tripRepository.findByParticipantIdsContaining(userId);
        joinedTrips.forEach(trip -> trip.setIsOrganizer(false)); // <-- Added "Is" here

        List<Trip> allMyTrips = new ArrayList<>();
        allMyTrips.addAll(organizedTrips);
        allMyTrips.addAll(joinedTrips);

        return ResponseEntity.ok(allMyTrips);
    }

    // ==========================================
    // JOIN REQUESTS & WAITING ROOM API (RESTORED)
    // ==========================================

    // 1. User clicks "Join" on the Discover page
    @PostMapping("/{tripId}/join")
    public ResponseEntity<String> requestToJoin(@PathVariable String tripId, Authentication authentication) {
        String userEmail = authentication.getName();
        tripService.requestToJoinTrip(tripId, userEmail);
        return ResponseEntity.ok("Join request sent to the organizer!");
    }

    // 2. Admin views their waiting room
    @GetMapping("/{tripId}/requests")
    public ResponseEntity<List<com.squadx.goout.Dto.UserSummaryDto>> getPendingRequests(@PathVariable String tripId, Authentication authentication) {
        String adminEmail = authentication.getName();
        List<com.squadx.goout.Dto.UserSummaryDto> pendingUsers = tripService.getPendingRequests(tripId, adminEmail);
        return ResponseEntity.ok(pendingUsers);
    }

    // 3. Admin accepts or rejects a request
    @PutMapping("/{tripId}/requests/{requesterId}")
    public ResponseEntity<String> resolveRequest(
            @PathVariable String tripId,
            @PathVariable String requesterId,
            @RequestParam String status, // Passed as ?status=ACCEPTED or ?status=REJECTED
            Authentication authentication) {

        String adminEmail = authentication.getName();
        tripService.resolveJoinRequest(tripId, requesterId, status, adminEmail);

        return ResponseEntity.ok("User request was " + status);
    }

    // 4. View official participants
    @GetMapping("/{tripId}/participants")
    public ResponseEntity<List<String>> getTripParticipants(@PathVariable String tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        return ResponseEntity.ok(trip.getParticipantIds());
    }
}