package com.squadx.goout.Controller;

import com.squadx.goout.Dto.TripResponseDto;
import com.squadx.goout.Dto.UserSummaryDto;
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

    // 1. Create a new trip
    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        trip.setOrganizerId(user.getId());
        Trip savedTrip = tripRepository.save(trip);
        return ResponseEntity.ok(savedTrip);
    }

    // 2. Complete a trip (Delegated to Service)
    @PatchMapping("/{tripId}/complete")
    public ResponseEntity<Trip> completeTrip(@PathVariable String tripId, Authentication authentication) {
        String userEmail = authentication.getName();
        // The Service handles all the heavy lifting and the Social Post creation now!
        Trip completedTrip = tripService.completeTrip(tripId, userEmail);
        return ResponseEntity.ok(completedTrip);
    }

    // 3. Get detailed view of a single trip
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

    // 4. Discovery Feed
    @GetMapping("/public")
    public ResponseEntity<List<Trip>> getPublicTrips() {
        return ResponseEntity.ok(tripRepository.findByIsPublicTrue());
    }

    // 5. My Trips List
    @GetMapping("/my-trips")
    public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String userId = user.getId();
        List<Trip> allMyTrips = new ArrayList<>();

        List<Trip> organized = tripRepository.findByOrganizerId(userId);
        organized.forEach(t -> t.setIsOrganizer(true));
        allMyTrips.addAll(organized);

        List<Trip> joined = tripRepository.findByParticipantIdsContaining(userId);
        joined.forEach(t -> t.setIsOrganizer(false));
        allMyTrips.addAll(joined);

        return ResponseEntity.ok(allMyTrips);
    }

    // 6. Join Request Operations
    @PostMapping("/{tripId}/join")
    public ResponseEntity<String> requestToJoin(@PathVariable String tripId, Authentication authentication) {
        tripService.requestToJoinTrip(tripId, authentication.getName());
        return ResponseEntity.ok("Join request sent!");
    }

    @GetMapping("/{tripId}/requests")
    public ResponseEntity<List<UserSummaryDto>> getPendingRequests(@PathVariable String tripId, Authentication authentication) {
        return ResponseEntity.ok(tripService.getPendingRequests(tripId, authentication.getName()));
    }

    @PutMapping("/{tripId}/requests/{requesterId}")
    public ResponseEntity<String> resolveRequest(
            @PathVariable String tripId,
            @PathVariable String requesterId,
            @RequestParam String status,
            Authentication authentication) {

        tripService.resolveJoinRequest(tripId, requesterId, status, authentication.getName());
        return ResponseEntity.ok("Request updated to " + status);
    }
}