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

        if (trip.getParticipantIds() == null) {
            trip.setParticipantIds(new ArrayList<>());
        }
        if (!trip.getParticipantIds().contains(user.getId())) {
            trip.getParticipantIds().add(user.getId());
        }

        Trip savedTrip = tripRepository.save(trip);
        return ResponseEntity.ok(savedTrip);
    }

    // Global Feed
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

        int likeCount = (trip.getLikedBy() != null) ? trip.getLikedBy().size() : 0;
        boolean isLiked = (trip.getLikedBy() != null) && trip.getLikedBy().contains(currentUserId);

        String userStatus = "NONE";
        if (trip.getOrganizerId() != null && trip.getOrganizerId().equals(currentUserId)) {
            userStatus = "ORGANIZER";
        } else if (trip.getParticipantIds() != null && trip.getParticipantIds().contains(currentUserId)) {
            userStatus = "APPROVED";
        } else if (trip.getPendingJoinRequests() != null && trip.getPendingJoinRequests().contains(currentUserId)) {
            userStatus = "PENDING";
        }

        TripResponseDto responseDto = new TripResponseDto(
                trip.getId(), trip.getTitle(), trip.getDescription(), trip.getDestinations(),
                trip.getImageUrl(), trip.getStartDate(), trip.getEndDate(), trip.getMinBudget(),
                trip.getMaxBudget(), trip.getMaxParticipants(), trip.getOrganizerId(),
                trip.getStatus(),
                trip.getGalleryImages(),
                likeCount,
                isLiked,
                userStatus,
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

    // 5. My Trips List (🌟 FIXED THE DUPLICATE LOGIC HERE!)
    @GetMapping("/my-trips")
    public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String userId = user.getId();
        List<Trip> allMyTrips = new ArrayList<>();

        // 1. Add all trips where the user is the Organizer
        List<Trip> organized = tripRepository.findByOrganizerId(userId);
        if (organized != null) {
            organized.forEach(t -> {
                t.setIsOrganizer(true);
                allMyTrips.add(t);
            });
        }

        // 2. Add trips where they are participating, BUT filter out the ones they organize!
        List<Trip> joined = tripRepository.findByParticipantIdsContaining(userId);
        if (joined != null) {
            joined.forEach(t -> {
                // 🌟 THE FIX: Only add it to the 'Joined' list if they are NOT the organizer
                if (t.getOrganizerId() != null && !t.getOrganizerId().equals(userId)) {
                    t.setIsOrganizer(false);
                    allMyTrips.add(t);
                }
            });
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

    // 7. Toggle Like Endpoint
    @PostMapping("/{tripId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable String tripId, Authentication authentication) {
        String userEmail = authentication.getName();
        tripService.toggleLike(tripId, userEmail);
        return ResponseEntity.ok("Trip like status toggled");
    }

    // 8. Delete Trip Endpoint
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrip(@PathVariable String id, Authentication authentication) {
        String userEmail = authentication.getName();
        tripService.deleteTrip(id, userEmail);
        return ResponseEntity.ok("Trip deleted successfully");
    }
}