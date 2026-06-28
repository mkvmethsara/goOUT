package com.squadx.goout.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts") // Tells MongoDB to create a new "posts" collection
public class Post {

    @Id
    private String id;

    private String content;
    private String location;
    private String imageUrl;

    // We only save the User's ID here. We will fetch their name and picture dynamically later!
    private String authorId;

    private LocalDateTime createdAt;

    // ADDED: List of User IDs who liked this post
    private List<String> likedBy = new ArrayList<>();
}