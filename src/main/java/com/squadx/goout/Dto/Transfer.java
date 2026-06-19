package com.squadx.goout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    private String fromUserId; // Changed to String for MongoDB!
    private String toUserId;   // Changed to String for MongoDB!
    private BigDecimal amount;
}