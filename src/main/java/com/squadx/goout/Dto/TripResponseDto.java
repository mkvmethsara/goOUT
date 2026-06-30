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

    // 🌟 ADDED: The full organizer details for Vishwa's feed!
    private TripMemberDto organizer;

    // The fully populated list of members
    private List<TripMemberDto> joinedMembers;

    /**
     * Inner DTO strictly for sending safe user data for the trip members list
     */
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