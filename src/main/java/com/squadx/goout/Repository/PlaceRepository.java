package com.squadx.goout.Repository;

import com.squadx.goout.Entity.Place;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends MongoRepository<Place, String> {

    // Finds all places associated with a specific trip, newest first
    List<Place> findByTripIdOrderByCreatedAtDesc(String tripId);
}