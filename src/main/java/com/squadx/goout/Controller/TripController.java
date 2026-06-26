package com.squadx.goout.Controller;

import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Repository.TripRepository;
import com.squadx.goout.Service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripRepository tripRepository;
    private final TripService tripService;

    // --- HASHEN'S METHODS (Keep these intact so the files don't conflict) ---

    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip, Authentication authentication) {
        String userEmail = authentication.getName();
        // Hashen will implement the logic here
        Trip savedTrip = tripRepository.save(trip);
        return ResponseEntity.ok(savedTrip);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Trip>> getPublicTrips() {
        // Hashen will implement findByIsPublicTrue() in Repo
        List<Trip> publicTrips = tripRepository.findAll(); // Temporary fallback
        return ResponseEntity.ok(publicTrips);
    }

    @GetMapping("/my-trips")
    public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {
        String userEmail = authentication.getName();
        // Hashen will implement the user lookup and findByOwnerId
        return ResponseEntity.ok(null);
    }

    // --- DEWNAKA'S METHODS (New Join/Participant Logic) ---

    /**
     * Endpoint: POST /api/v1/trips/{tripId}/join
     * Purpose: Allows the currently logged-in user to join a specific trip.
     */
    @PostMapping("/{tripId}/join")
    public ResponseEntity<String> joinTrip(@PathVariable String tripId, Authentication authentication) {
        // Get the email of the currently logged-in user from the security context
        String userEmail = authentication.getName();

        // Call the service to handle the database logic
        tripService.joinTrip(tripId, userEmail);

        return ResponseEntity.ok("User joined trip successfully");
    }

    /**
     * Endpoint: GET /api/v1/trips/{tripId}/participants
     * Purpose: Returns the list of User IDs participating in the trip.
     */
    @GetMapping("/{tripId}/participants")
    public ResponseEntity<List<String>> getTripParticipants(@PathVariable String tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        return ResponseEntity.ok(trip.getParticipantIds());
    }
}
