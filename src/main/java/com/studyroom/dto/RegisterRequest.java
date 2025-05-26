package com.studyroom.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String name;
    private String studentId;
    // Getters and setters
}
