package com.squadx.goout.Service;

import com.squadx.goout.Dto.Transfer;
import com.squadx.goout.Dto.UserBalance;
import com.squadx.goout.Entity.Expense;
import com.squadx.goout.Entity.Trip;
import com.squadx.goout.Repository.ExpenseRepository;
import com.squadx.goout.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final SettlementService settlementService;

    // 1. Save a new receipt to MongoDB
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    // 🌟 NEW: Fetch the basic ledger for Phase 1 MVP
    public List<Expense> getAllExpensesForTrip(String tripId) {
        // Our repository already has this method from when we built the Math Engine!
        return expenseRepository.findByTripId(tripId);
    }

    // 2. The Math Engine for the Figma Dashboard
    public Map<String, Object> getTripExpenseDashboard(String tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        List<Expense> expenses = expenseRepository.findByTripId(tripId);

        double totalExpenses = 0;
        for (Expense e : expenses) {
            totalExpenses += e.getAmount();
        }

        int totalTravelers = trip.getParticipantIds().size();
        // Prevent division by zero if it's just the organizer!
        if (totalTravelers == 0) {
            totalTravelers = 1;
        }

        double perPerson = totalExpenses / totalTravelers;

        // 🌟 NEW: Calculate the Budget Health Status!
        String budgetStatus = "UNKNOWN";
        double estimatedBudget = (trip.getMinBudget() != null) ? trip.getMinBudget() : 0.0;

        if (estimatedBudget > 0) {
            // Calculate what percentage of the budget has been spent
            double percentSpent = (totalExpenses / estimatedBudget) * 100;

            if (percentSpent > 100) {
                budgetStatus = "OVER_BUDGET";
            } else if (percentSpent > 85) {
                // If they have spent more than 85%, warn them they are getting close!
                budgetStatus = "ON_TRACK_WARNING";
            } else {
                budgetStatus = "UNDER_BUDGET";
            }
        }

        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("totalExpenses", totalExpenses);
        dashboardData.put("perPerson", perPerson);
        dashboardData.put("totalTravelers", totalTravelers);

        // Include the new math in the response!
        dashboardData.put("estimatedBudget", estimatedBudget);
        dashboardData.put("budgetStatus", budgetStatus);

        return dashboardData;
    }

    // 3. The Settlement Algorithm Data Prep
    public List<Transfer> calculateSettlementsForTrip(String tripId, List<String> participantIds) {

        List<Expense> tripExpenses = expenseRepository.findByTripId(tripId);

        // FIXED: Convert double to BigDecimal safely using valueOf()
        BigDecimal totalCost = tripExpenses.stream()
                .map(e -> BigDecimal.valueOf(e.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (participantIds.isEmpty()) return new ArrayList<>();
        BigDecimal perPersonShare = totalCost.divide(new BigDecimal(participantIds.size()), 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> amountPaidPerUser = new HashMap<>();
        for (String userId : participantIds) {
            amountPaidPerUser.put(userId, BigDecimal.ZERO);
        }

        for (Expense expense : tripExpenses) {
            // IMPORTANT: Assuming the variable in Expense.java is named 'userId'.
            String payerId = expense.getUserId();

            // Tech Lead optimization: Using modern Java's computeIfPresent to replace 4 lines with 1!
            amountPaidPerUser.computeIfPresent(payerId,
                    (key, currentPaid) -> currentPaid.add(BigDecimal.valueOf(expense.getAmount()))
            );
        }

        List<UserBalance> balances = new ArrayList<>();
        for (String userId : participantIds) {
            BigDecimal paid = amountPaidPerUser.get(userId);
            BigDecimal balance = paid.subtract(perPersonShare);
            balances.add(new UserBalance(userId, "Regular", balance));
        }

        return settlementService.calculateSettlements(balances);
    }
}