package com.squadx.goout.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String destinations; // Plural
    private String imageUrl;

    private LocalDate startDate; // FIXED TYPO: changed from stratDate
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
}