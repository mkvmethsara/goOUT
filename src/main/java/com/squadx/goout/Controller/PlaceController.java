package com.squadx.goout.Controller;

import com.squadx.goout.Entity.Place;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.PlaceRepository;
import com.squadx.goout.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    // 1. Add a Place to a Trip
    @PostMapping("/{tripId}/places")
    public ResponseEntity<Place> addPlaceToTrip(
            @PathVariable String tripId,
            @RequestBody Place place,
            Authentication authentication) {

        // 1. Identify who is posting the place
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Attach the required IDs
        place.setTripId(tripId);
        place.setAuthorId(currentUser.getId());

        // Let's attach their name too, so the frontend can easily display it!
        String authorName = currentUser.getFirstName() + " " + (currentUser.getLastName() != null ? currentUser.getLastName() : "");
        place.setAuthorName(authorName.trim());

        // 3. Save to MongoDB
        Place savedPlace = placeRepository.save(place);
        return ResponseEntity.ok(savedPlace);
    }

    // 2. Get All Places for a Trip
    @GetMapping("/{tripId}/places")
    public ResponseEntity<List<Place>> getPlacesForTrip(@PathVariable String tripId) {

        // Fetch them all, sorted newest first!
        List<Place> places = placeRepository.findByTripIdOrderByCreatedAtDesc(tripId);
        return ResponseEntity.ok(places);
    }
}