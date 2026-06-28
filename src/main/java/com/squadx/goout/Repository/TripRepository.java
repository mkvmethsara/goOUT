package com.squadx.goout.Repository;

import com.squadx.goout.Entity.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {

    // ==========================================
    // EXISTING METHODS (Do not delete these!)
    // ==========================================
    List<Trip> findByOrganizerId(String organizerId);
    List<Trip> findByDestinationsContainingIgnoreCase(String destination);

    // NEW: Helps us find trips the user joined!
    List<Trip> findByParticipantIdsContaining(String participantId);

    // ==========================================
    // HASHEN'S NEW METHODS (Core Trip API)
    // ==========================================

    // Finds all trips marked as public for the discovery feed
    List<Trip> findByIsPublicTrue();
}