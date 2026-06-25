package com.squadx.goout.Service;

import com.squadx.goout.Entity.Expense;
import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Repository.ExpenseRepository;
import com.squadx.goout.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;

    // 1. Save a new receipt to MongoDB
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    // 2. The Math Engine for the Figma Dashboard
    public Map<String, Object> getTripExpenseDashboard(String tripId) {

        // Step A: Fetch the trip to count the travelers
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Step B: Fetch all receipts for this specific trip
        List<Expense> expenses = expenseRepository.findByTripId(tripId);

        // Step C: Calculate the Total Cost
        double totalExpenses = 0;
        for (Expense e : expenses) {
            totalExpenses += e.getAmount();
        }

        // Step D: Calculate Per-Person Average Safely
        int totalTravelers = trip.getParticipantIds().size();

        // Safety check: Prevent the server from crashing if no one has joined yet (Division by Zero)
        if (totalTravelers == 0) {
            totalTravelers = 1;
        }

        double perPerson = totalExpenses / totalTravelers;

        // Step E: Package the exact numbers the React team needs for their UI
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("totalExpenses", totalExpenses);
        dashboardData.put("perPerson", perPerson);
        dashboardData.put("totalTravelers", totalTravelers);

        return dashboardData;
    }
}