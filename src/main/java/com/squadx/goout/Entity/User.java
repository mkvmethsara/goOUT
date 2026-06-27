package com.squadx.goout.Entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

//Auto generates getters and setters
@Data

//Create empty Con
@NoArgsConstructor

//Crate Con with all fields
@AllArgsConstructor

@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String profileImageUrl;
    private String travelerType;
    private String bio;
    private String location;
    private String avatarUrl;

    private LocalDateTime joinedDate = LocalDateTime.now();
}
