package com.squadx.goout.Controller;


import com.squadx.goout.Dto.LoginRequest;
import com.squadx.goout.Entity.User;
import com.squadx.goout.Service.JwtService;
import com.squadx.goout.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")

//Auto-injects UserService
@RequiredArgsConstructor

public class AuthController {

    private final UserService userService;

    //Endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        try{
            //send the raw request data to Service
            User savedUser = userService.registerNewUser(user);

            //Return HTTP 201 Created status code with save user document
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);

        } catch (RuntimeException e){
            //If rules fail , return 404 Bad Request error message
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request){

        Optional<User> userOptional = userService.getUserByEmail(request.getEmail());

        if (userOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: User not found.");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Invalid password.");
        }

        String token = jwtService.generateToken(user.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("massage", "Login successful!");

        return ResponseEntity.ok(response);
    }

}
