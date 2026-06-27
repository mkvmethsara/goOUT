package com.squadx.goout.Controller;

import com.squadx.goout.Dto.UserProfileUpdateDto;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

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
}