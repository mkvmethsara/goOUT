package com.squadx.goout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String bio;
    private String location;
    private String avatarUrl;
}