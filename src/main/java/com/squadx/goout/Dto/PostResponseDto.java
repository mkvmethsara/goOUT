package com.squadx.goout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private String id;
    private String content;
    private String location;
    private String imageUrl;
    private LocalDateTime createdAt;

    private AuthorDto author;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private String name;
        private String avatarUrl;
    }
}