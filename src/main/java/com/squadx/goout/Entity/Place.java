package com.squadx.goout.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "places")
public class Place {

    @Id
    private String id;

    // Links this place to a specific trip
    private String tripId;

    // Who recommended this place?
    private String authorId;
    // We can save the author's name to avoid an extra DB lookup later if we want to optimize!
    private String authorName;

    private String name;
    private String category;
    private String tip;

    private LocalDateTime createdAt = LocalDateTime.now();
}