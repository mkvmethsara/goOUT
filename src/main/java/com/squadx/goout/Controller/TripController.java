package com.squadx.goout.Controller;

import com.squadx.goout.Dto.TripResponseDto;
import com.squadx.goout.Dto.UserSummaryDto;
import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.TripRepository;
import com.squadx.goout.Repository.UserRepository;
import com.squadx.goout.Service.TripService;
import jakarta.validation.Valid; // 🌟 ADDED: Jakarta Validation Import
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
    // 🗑️ REMOVED PostRepository because we don't need Auto-Posts anymore!

    // 1. Create a new trip (Cleaned up: No more Auto-Posts)
    @PostMapping
    public ResponseEntity<Trip> createTrip(@Valid @RequestBody Trip trip, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        trip.setOrganizerId(user.getId());

        // 🌟 ADDED: Make sure the organizer gets a seat on their own trip! (1/10 members fix)
        if (trip.getParticipantIds() == null) {
            trip.setParticipantIds(new ArrayList<>());
        }
        if (!trip.getParticipantIds().contains(user.getId())) {
            trip.getParticipantIds().add(user.getId());
        }

        // 🌟 THE FIX: We must actually save the trip to MongoDB!
        Trip savedTrip = tripRepository.save(trip);

        return ResponseEntity.ok(savedTrip);
    }

    // 🌟 UPDATED: The Global Feed now takes an optional 'search' parameter!
    @GetMapping
    public ResponseEntity<List<TripResponseDto>> getGlobalUpcomingFeed(
            @RequestParam(required = false) String search,
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<TripResponseDto> feed = tripService.getGlobalUpcomingFeed(userEmail, search);
        return ResponseEntity.ok(feed);
    }

    // 2. Complete a trip
    @PatchMapping("/{tripId}/complete")
    public ResponseEntity<Trip> completeTrip(@PathVariable String tripId, Authentication authentication) {
        String userEmail = authentication.getName();
        Trip completedTrip = tripService.completeTrip(tripId, userEmail);
        return ResponseEntity.ok(completedTrip);
    }

    // 3. Get detailed view of a single trip
    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDto> getTripDetails(@PathVariable String id, Authentication authentication) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String currentUserId = currentUser.getId();

        List<TripResponseDto.TripMemberDto> populatedMembers = new ArrayList<>();
        if (trip.getParticipantIds() != null) {
            for (String userId : trip.getParticipantIds()) {
                userRepository.findById(userId).ifPresent(user ->
                        populatedMembers.add(new TripResponseDto.TripMemberDto(
                                user.getId(), user.getFirstName(), user.getLastName(), user.getAvatarUrl()
                        ))
                );
            }
        }

        User org = userRepository.findById(trip.getOrganizerId()).orElse(null);
        TripResponseDto.TripMemberDto organizerDto = null;
        if (org != null) {
            organizerDto = new TripResponseDto.TripMemberDto(
                    org.getId(), org.getFirstName(), org.getLastName(), org.getAvatarUrl()
            );
        }

        // 🌟 ADDED: Calculate Like Metrics for the details page
        int likeCount = (trip.getLikedBy() != null) ? trip.getLikedBy().size() : 0;
        boolean isLiked = (trip.getLikedBy() != null) && trip.getLikedBy().contains(currentUserId);

        TripResponseDto responseDto = new TripResponseDto(
                trip.getId(), trip.getTitle(), trip.getDescription(), trip.getDestinations(),
                trip.getImageUrl(), trip.getStartDate(), trip.getEndDate(), trip.getMinBudget(),
                trip.getMaxBudget(), trip.getMaxParticipants(), trip.getOrganizerId(),
                trip.getStatus(),
                likeCount, // <-- Included here!
                isLiked,   // <-- Included here!
                organizerDto,
                populatedMembers
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
        if (organized != null) {
            organized.forEach(t -> t.setIsOrganizer(true));
            allMyTrips.addAll(organized);
        }

        List<Trip> joined = tripRepository.findByParticipantIdsContaining(userId);
        if (joined != null) {
            joined.forEach(t -> t.setIsOrganizer(false));
            allMyTrips.addAll(joined);
        }

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

    // 🌟 7. NEW: Toggle Like Endpoint!
    @PostMapping("/{tripId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable String tripId, Authentication authentication) {
        String userEmail = authentication.getName();
        tripService.toggleLike(tripId, userEmail);
        return ResponseEntity.ok("Trip like status toggled");
    }

    // 🌟 8. NEW: Delete Trip Endpoint! (Fixes the 405 Error)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrip(@PathVariable String id, Authentication authentication) {
        String userEmail = authentication.getName();
        tripService.deleteTrip(id, userEmail);
        return ResponseEntity.ok("Trip deleted successfully");
    }
}