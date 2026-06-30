package com.squadx.goout.Controller;

import com.squadx.goout.Entity.Expense;
import com.squadx.goout.Service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; // 🌟 ADDED: We need this to return the array of expenses!
import java.util.Map;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // 2. Get Dashboard Data (Math Engine)
    @GetMapping("/trip/{tripId}/dashboard")
    public ResponseEntity<Map<String, Object>> getTripDashboardData(@PathVariable String tripId) {
        try {
            Map<String, Object> dashboardData = expenseService.getTripExpenseDashboard(tripId);
            return ResponseEntity.ok(dashboardData);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Endpoint 3: Get the Trip Ledger (MVP Phase 1)
     * URL: GET http://localhost:8080/api/v1/expenses/trip/{tripId}
     * Frontend Team Goal: Fetch all expenses for a specific trip to display in the list.
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<Expense>> getTripLedger(@PathVariable String tripId) {
        List<Expense> ledger = expenseService.getAllExpensesForTrip(tripId);
        return ResponseEntity.ok(ledger);
    }
}