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
@Document(collection = "expenses")
public class Expense {

    @Id
    private String id;

    private String tripId;
    private String title;
    private double amount;
    private String category;

    // The display name the frontend sends for the MVP (e.g., "Vishwa")
    private String paidBy;

    // The actual database ID of the user (We will need this later for the Math Engine!)
    private String userId;

    private LocalDateTime createdAt = LocalDateTime.now();
}