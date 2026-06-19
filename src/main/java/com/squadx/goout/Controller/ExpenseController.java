package com.squadx.goout.Controller;

import com.squadx.goout.Entity.Expense;
import com.squadx.goout.Service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Endpoint 1: Add a New Receipt
     * URL: POST http://localhost:8080/api/expenses
     * Frontend Team Goal: When a user clicks "Add Expense" on the Figma form, send the JSON here.
     */
    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
        Expense savedExpense = expenseService.addExpense(expense);
        return new ResponseEntity<>(savedExpense, HttpStatus.CREATED);
    }

    /**
     * Endpoint 2: Get the Dashboard Math
     * URL: GET http://localhost:8080/api/expenses/trip/{tripId}/dashboard
     * Frontend Team Goal: Call this URL when the page loads to get the Total Cost and Per Person averages.
     */
    @GetMapping("/trip/{tripId}/dashboard")
    public ResponseEntity<Map<String, Object>> getTripDashboardData(@PathVariable String tripId) {
        try {
            Map<String, Object> dashboardData = expenseService.getTripExpenseDashboard(tripId);
            return ResponseEntity.ok(dashboardData);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}