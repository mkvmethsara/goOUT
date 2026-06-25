package com.squadx.goout.Dto;


import lombok.Data;

//for getter and setters
@Data
public class LoginRequest {
    private String email;
    private String password;
}
