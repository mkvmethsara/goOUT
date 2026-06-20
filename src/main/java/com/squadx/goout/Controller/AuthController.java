package com.squadx.goout.Controller;

import com.squadx.goout.Dto.LoginRequest;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.UserRepository;
import com.squadx.goout.Service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. The NEW Register Endpoint
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {

        // Scramble the password before saving it to MongoDB!
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save the user
        userRepository.save(user);

        // Generate a token for the new user
        String jwtToken = jwtService.generateToken(user.getEmail());

        return ResponseEntity.ok(jwtToken);
    }

    // 2. The Existing Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        // Verify the email and password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Generate a token
        String jwtToken = jwtService.generateToken(request.getEmail());

        return ResponseEntity.ok(jwtToken);
    }
}