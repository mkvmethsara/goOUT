package com.squadx.goout.Controller;

import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.TripRepository;
import com.squadx.goout.Repository.UserRepository;
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
    private final UserRepository userRepository;

    // ==========================================
    // HASHEN'S METHODS (Completed Core API)
    // ==========================================

    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip, Authentication authentication) {
        String userEmail = authentication.getName();

        // Find the user to get their MongoDB ID
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set this user as the Organizer!
        trip.setOrganizerId(user.getId());

        Trip savedTrip = tripRepository.save(trip);
        return ResponseEntity.ok(savedTrip);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Trip>> getPublicTrips() {
        List<Trip> publicTrips = tripRepository.findAll();
        return ResponseEntity.ok(publicTrips);
    }

    @GetMapping("/my-trips")
    public ResponseEntity<List<Trip>> getMyTrips(Authentication authentication) {
        String userEmail = authentication.getName();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return only the trips organized by this specific user
        List<Trip> myTrips = tripRepository.findByOrganizerId(user.getId());
        return ResponseEntity.ok(myTrips);
    }

    // ==========================================
    // DEWNAKA'S METHODS (Trip Participants)
    // ==========================================

    @PostMapping("/{tripId}/join")
    public ResponseEntity<String> joinTrip(@PathVariable String tripId, Authentication authentication) {
        String userEmail = authentication.getName();
        tripService.joinTrip(tripId, userEmail);
        return ResponseEntity.ok("User joined trip successfully");
    }

    @GetMapping("/{tripId}/participants")
    public ResponseEntity<List<String>> getTripParticipants(@PathVariable String tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        return ResponseEntity.ok(trip.getParticipantIds());
    }
}