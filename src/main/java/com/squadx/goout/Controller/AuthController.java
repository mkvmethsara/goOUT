package com.squadx.goout.Controller;

import com.squadx.goout.Dto.LoginRequest;
import com.squadx.goout.Dto.VerifyRequest;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.UserRepository;
import com.squadx.goout.Service.JwtService;
import com.squadx.goout.Service.EmailService; // 🌟 ADDED
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // 🌟 ADDED

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {

        // 🌟 NEW SECURITY CHECK: Prevent duplicate emails!
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists!");
        }

        // 1. Scramble the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. Generate a 6-digit OTP
        String generatedOtp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(generatedOtp);
        user.setIsVerified(false);

        // 3. Save the unverified user
        userRepository.save(user);

        // 4. THE FAILSAFE: Always print to the console! (Methsara's Idea 💡)
        System.out.println("\n========================================================");
        System.out.println("🚨 OTP GENERATED (IDE FALLBACK ACTIVE)!");
        System.out.println("📩 TO: " + user.getEmail());
        System.out.println("🔑 OTP CODE: " + generatedOtp);
        System.out.println("========================================================\n");

        // 5. 🌟 NEW: ATTEMPT REAL EMAIL
        try {
            emailService.sendOtpEmail(user.getEmail(), generatedOtp);
            System.out.println("✅ Real email successfully sent to " + user.getEmail());
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Real email failed, but OTP is in console. Error: " + e.getMessage());
        }

        // Notice we do NOT return the JWT token here anymore!
        return ResponseEntity.ok("User registered successfully. Please check your email for the OTP.");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody VerifyRequest request) {

        // 1. Find the user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if the OTP matches
        if (user.getOtp() != null && user.getOtp().equals(request.getOtp())) {

            // Success! Mark as verified and clear the OTP so it can't be used again
            user.setIsVerified(true);
            user.setOtp(null);
            userRepository.save(user);

            // NOW we give them the JWT token!
            String jwtToken = jwtService.generateToken(user.getEmail());
            return ResponseEntity.ok(jwtToken);
        }

        throw new RuntimeException("Invalid OTP code!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        // NEW SECURITY CHECK: Don't let them log in if they aren't verified!
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getIsVerified() == null || !user.getIsVerified()) {
            throw new RuntimeException("Please verify your email address before logging in!");
        }

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