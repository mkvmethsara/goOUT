package com.squadx.goout.Controller;

import com.squadx.goout.Dto.UserProfileUpdateDto;
import com.squadx.goout.Dto.ChangePasswordRequest; // 🌟 ADDED
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.UserRepository;
import com.squadx.goout.Repository.TripRepository; // 🌟 ADDED
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder; // 🌟 ADDED
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 🌟 ADDED to hash passwords
    private final TripRepository tripRepository;   // 🌟 ADDED for cascading deletes

    /**
     * Endpoint: GET /api/v1/users/me
     * Purpose: Returns the profile data of the user currently holding the valid JWT token.
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUserProfile(Authentication authentication) {

        // 1. Get the email of the person who just passed the Bouncer
        String userEmail = authentication.getName();

        // 2. Look them up in MongoDB
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found in database"));

        // 3. SECURITY RULE: We never want to send the scrambled password back to the frontend!
        // We set it to null just for this response. (It does not delete it from the database).
        currentUser.setPassword(null);

        // 4. Return the clean profile data to React
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Endpoint: PUT /api/v1/users/me
     * Purpose: Updates the bio and location of the currently logged-in user.
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateUserProfile(
            @RequestBody UserProfileUpdateDto updateDto,
            Authentication authentication) {

        // 1. Identify the user securely from their token
        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found in database"));

        // 2. Update the fields only if the frontend actually sent them
        if (updateDto.getBio() != null) {
            currentUser.setBio(updateDto.getBio());
        }
        if (updateDto.getLocation() != null) {
            currentUser.setLocation(updateDto.getLocation());
        }
        // ADDED: Check and update the avatar URL if Vishwa sends it!
        if (updateDto.getAvatarUrl() != null) {
            currentUser.setAvatarUrl(updateDto.getAvatarUrl());
        }

        // 3. Save the changes to MongoDB
        userRepository.save(currentUser);

        // 4. SECURITY RULE: Null out the password before sending the updated user back!
        currentUser.setPassword(null);

        return ResponseEntity.ok(currentUser);
    }

    // 🌟 NEW: Change Password Endpoint
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Verify the old password matches what is in the database
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            // Returns a 400 Bad Request if the old password was wrong
            return ResponseEntity.badRequest().body("Incorrect current password.");
        }

        // 2. Hash the new password and save it
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        return ResponseEntity.ok("Password updated successfully.");
    }

    // 🌟 NEW: Delete Account Endpoint
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🧹 CASCADING DELETE: Prevent "Ghost Trips" by deleting trips this user organized
        tripRepository.deleteAll(tripRepository.findByOrganizerId(currentUser.getId()));

        // Finally, delete the user themselves
        userRepository.delete(currentUser);

        return ResponseEntity.ok("Account and associated data permanently deleted.");
    }
}