package com.squadx.goout.Controller;


import com.squadx.goout.Entity.User;
import com.squadx.goout.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
