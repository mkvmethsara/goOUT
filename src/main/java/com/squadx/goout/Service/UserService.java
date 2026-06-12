package com.squadx.goout.Service;


import com.squadx.goout.Entity.User;
import com.squadx.goout.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

//Create UserRepo Con(No need for @Autowired)
@RequiredArgsConstructor


public class UserService {

    private final UserRepository userRepository;

    //Registering a brand-new traveler in the system.
    public User registerNewUser(User user){

        //Get the email and also check its already exists
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()){
            throw new RuntimeException("Email address already exists!");
        }

        //save the clear-text password
        user.setPassword(user.getPassword());

        //Save the user direclty into MongoDB
        return userRepository.save(user);
    }

    //Get the user profile by their unique email address
    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
