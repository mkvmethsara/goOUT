package com.squadx.goout.Controller;

import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.TripRepository;
import com.squadx.goout.Repository.UserRepository;
import com.squadx.goout.Service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripRepository tripRepository;
    private final TripService tripService;
    private final UserRepository userRepository;

    // ==========================================
    // CORE TRIP API
    // ==========================================

    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        trip.setOrganizerId(user.getId());
        Trip savedTrip = tripRepository.save(trip);
        return ResponseEntity.ok(savedTrip);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Trip>> getPublicTrips(Authentication authentication) {
        // Find the current user
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get ALL trips
        List<Trip> allTrips = tripRepository.findAll();

        // Filter out trips where the current user is the organizer
        List<Trip> discoverFeed = allTrips.stream()
                .filter(trip -> !currentUser.getId().equals(trip.getOrganizerId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(discoverFeed);
    }

    @GetMapping("/my-trips")
    public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Trip> myTrips = tripRepository.findByOrganizerId(user.getId());
        return ResponseEntity.ok(myTrips);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrip(@PathVariable String id, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getOrganizerId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this trip!");
        }

        tripRepository.deleteById(id);
        return ResponseEntity.ok("Trip deleted successfully");
    }

    // ==========================================
    // JOIN REQUESTS & WAITING ROOM API
    // ==========================================

    // 1. User clicks "Join" on the Discover page
    @PostMapping("/{tripId}/join")
    public ResponseEntity<String> requestToJoin(@PathVariable String tripId, Authentication authentication) {
        String userEmail = authentication.getName();
        tripService.requestToJoinTrip(tripId, userEmail);
        return ResponseEntity.ok("Join request sent to the organizer!");
    }

    // 2. Admin views their waiting room (UPDATED)
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