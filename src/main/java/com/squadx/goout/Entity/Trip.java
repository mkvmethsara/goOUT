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

    // Bulletproofing date parsing from React!
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // 🚨 UPGRADED to Wrapper classes (Double/Integer) so Jackson accepts 'null' safely!
    private Double minBudget;
    private Double maxBudget;
    private Integer maxParticipants;

    private String organizerId;

    // The Official Members
    private List<String> participantIds = new ArrayList<>();

    // The Waiting Room
    private List<String> pendingJoinRequests = new ArrayList<>();

    // 🌟 ADDED: List of User IDs who liked this trip
    private List<String> likedBy = new ArrayList<>();

    // 🚨 UPDATED: Default to UPCOMING for the new frontend feed tabs!
    private String status = "UPCOMING";

    // 🚨 UPGRADED to Boolean object to prevent null-mapping crashes!
    private Boolean isPublic = true;

    // UPGRADED to Boolean object
    @Transient
    @JsonProperty("isOrganizer")
    private Boolean isOrganizer;
}