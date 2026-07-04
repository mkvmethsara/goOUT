package com.squadx.goout.Entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotBlank(message = "Trip title is required and cannot be empty.")
    private String title;

    @NotBlank(message = "Trip description is required and cannot be empty.")
    private String description;

    @NotBlank(message = "At least one destination is required.")
    private String destinations;

    // Optional field, so no validation needed
    private String imageUrl;

    @NotNull(message = "Start date is required.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Minimum budget is required.")
    @PositiveOrZero(message = "Budget cannot be negative.")
    private Double minBudget;

    @NotNull(message = "Maximum budget is required.")
    @PositiveOrZero(message = "Budget cannot be negative.")
    private Double maxBudget;

    @NotNull(message = "Maximum participants count is required.")
    @PositiveOrZero(message = "Participants count must be zero or greater.")
    private Integer maxParticipants;

    private String organizerId;

    // The Official Members
    private List<String> participantIds = new ArrayList<>();

    // The Waiting Room
    private List<String> pendingJoinRequests = new ArrayList<>();

    // List of User IDs who liked this trip
    private List<String> likedBy = new ArrayList<>();

    // Default to UPCOMING for the new frontend feed tabs!
    private String status = "UPCOMING";

    // UPGRADED to Boolean object to prevent null-mapping crashes!
    private Boolean isPublic = true;

    // UPGRADED to Boolean object
    @Transient
    @JsonProperty("isOrganizer")
    private Boolean isOrganizer;
}