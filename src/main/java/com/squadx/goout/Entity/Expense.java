package com.squadx.goout.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "expenses")

public class Expense {

    @Id
    private String id;

    // The ID of the specific trip this expense belongs to
    private String tripId;

    // The ID of the user who actually paid the bill
    private String paidByUserId;

    private String description;
    private double amount;



    private String category;

    private LocalDate date = LocalDate.now();
}