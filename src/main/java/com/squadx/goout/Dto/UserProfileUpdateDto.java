package com.squadx.goout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDto {
    private String bio;
    private String location;
    private String avatarUrl; // ADDED: Catcher for the new profile picture URL!
}