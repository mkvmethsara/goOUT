package com.squadx.goout.Config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Global Error Catcher for the entire Spring Boot Application.
 * This intercepts any crashed Java code and formats it into a clean,
 * readable JSON object (RFC 7807 Standard) for the React frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Catches generic RuntimeExceptions (e.g., when an entity is not found or a general error occurs).
     */
    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex) {
        // Create a structured ProblemDetail object with a 400 Bad Request status
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

        // Add standard ProblemDetail fields
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(URI.create("https://api.goout.com/errors/bad-request"));

        // Add custom fields for the frontend to use easily
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("customMessage", "An error occurred while processing your request.");

        return problemDetail;
    }

    /**
     * Catches incorrect passwords or invalid login attempts.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setType(URI.create("https://api.goout.com/errors/unauthorized"));
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    /**
     * Catches unauthorized access (e.g., trying to access a protected route without a valid token).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
        problemDetail.setTitle("Access Denied");
        problemDetail.setType(URI.create("https://api.goout.com/errors/forbidden"));
        problemDetail.setProperty("timestamp", Instant.now().toString());

        return problemDetail;
    }

    /**
     * The ultimate fallback. If an error happens that we didn't plan for, this catches it
     * instead of printing an ugly Java stack trace to the user's browser.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred.");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.goout.com/errors/internal-server-error"));
        problemDetail.setProperty("timestamp", Instant.now().toString());

        // In a real production app, we would log 'ex.getMessage()' to a secure server log here,
        // but we DO NOT send the raw exception message to the frontend for security reasons.

        return problemDetail;
    }
}