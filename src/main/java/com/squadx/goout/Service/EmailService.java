package com.squadx.goout.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("goout.squadx@gmail.com"); // Replace with your project email
        message.setTo(toEmail);
        message.setSubject("🌍 Welcome to GoOUT! Here is your Verification Code");

        message.setText("Hello Traveler!\n\n" +
                "Thank you for joining GoOUT. To complete your registration and secure your account, " +
                "please enter the following One-Time Password (OTP) in the app:\n\n" +
                "🔑 Your OTP: " + otp + "\n\n" +
                "Get ready for your next adventure!\n\n" +
                "- The GoOUT Team");

        // This will attempt to send the email across the internet
        mailSender.send(message);
    }
}