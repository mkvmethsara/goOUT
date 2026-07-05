package com.squadx.goout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripResponseDto {
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
    private String status;

    // 🌟 ADDED: Image Gallery for the Trip Overview Blog
    private List<String> galleryImages;

    // Like metrics for the frontend feed
    private int likeCount;
    private boolean isLikedByCurrentUser;

    // 🌟 ADDED: The new Quality of Life flag for the frontend!
    // Possible values: 'ORGANIZER', 'APPROVED', 'PENDING', 'NONE'
    private String currentUserStatus;

    // The full organizer details
    private TripMemberDto organizer;

    // The fully populated list of members
    private List<TripMemberDto> joinedMembers;

    // Jackson will automatically serialize these into the JSON response
    public String getType() {
        return "TRIP";
    }

    public boolean getIsTrip() {
        return true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripMemberDto {
        private String id;
        private String firstName;
        private String lastName;
        private String avatarUrl;
    }
}