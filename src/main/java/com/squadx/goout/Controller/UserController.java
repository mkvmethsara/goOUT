package com.squadx.goout.Controller;

import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
}