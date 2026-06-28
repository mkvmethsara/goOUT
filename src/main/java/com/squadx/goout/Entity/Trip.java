package com.squadx.goout.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Transient;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trips")
public class Trip {

    @Id
    private String id;

    private String title;
    private String description;
    private String destinations;
    private String imageUrl;

    private LocalDate startDate;
    private LocalDate endDate;

    private double minBudget;
    private double maxBudget;
    private int maxParticipants;

    private String organizerId;

    // The Official Members
    private List<String> participantIds = new ArrayList<>();

    // 🚨 NEW: The Waiting Room 🚨
    private List<String> pendingJoinRequests = new ArrayList<>();

    private String status = "ACTIVE";

    // 🚨 NEW: Added so Spring Boot can run findByIsPublicTrue() without crashing!
    private boolean isPublic = true; // Defaulting to true so they show up on the Discover page

    // 🚨 NEW: Not saved in DB, but passed to React to solve Vishwa's issue!
    @Transient
    @JsonProperty("isOrganizer")
    private boolean isOrganizer;
}