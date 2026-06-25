package com.squadx.goout.Repository;

import com.squadx.goout.Entity.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {

    List<Trip> findByOrganizerId(String organizerId);

    List<Trip> findByDestinationsContainingIgnoreCase(String destination);
}
