package com.squadx.goout.Service;

import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.TripRepository;
import com.squadx.goout.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public void joinTrip(String tripId, String userEmail) {
        // 1. Find the User by email (passed from the JWT token)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Find the Trip by its MongoDB ID
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // 3. Prevent duplicate joins (Check if user is already in the list)
        if (trip.getParticipantIds().contains(user.getId())) {
            throw new RuntimeException("User is already a participant in this trip");
        }

        // 4. Add the User's MongoDB ID string to the trip's participant list
        trip.getParticipantIds().add(user.getId());

        // 5. Save the updated Trip back to MongoDB
        tripRepository.save(trip);
    }
}