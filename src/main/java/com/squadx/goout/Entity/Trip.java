package com.squadx.goout.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Transient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    // 🚨 ADD THESE TWO ANNOTATIONS to fix the Date parsing error!
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private double minBudget;
    private double maxBudget;
    private int maxParticipants;

    private String organizerId;

    // The Official Members
    private List<String> participantIds = new ArrayList<>();

    // The Waiting Room
    private List<String> pendingJoinRequests = new ArrayList<>();

    private String status = "ACTIVE";

    // Helps us find public trips for the Discover feed
    private boolean isPublic = true;

    // Not saved in DB, but passed to React for UI categorization
    @Transient
    @JsonProperty("isOrganizer")
    private boolean isOrganizer;
}