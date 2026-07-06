package com.squadx.goout.Controller;

import com.squadx.goout.Entity.Notification;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.UserRepository;
import com.squadx.goout.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // The exact endpoint the frontend requested!
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(Authentication authentication) {
        String userEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> myNotifications = notificationService.getUserNotifications(currentUser.getId());

        return ResponseEntity.ok(myNotifications);
    }
}