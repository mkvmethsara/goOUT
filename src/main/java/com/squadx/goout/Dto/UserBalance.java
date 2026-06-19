package com.squadx.goout.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {
    private String userId; // Changed to String for MongoDB!
    private String travelerType;
    private BigDecimal balance;
}