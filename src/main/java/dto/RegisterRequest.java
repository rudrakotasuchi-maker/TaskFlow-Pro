package com.taskflow.taskflowpro.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String fullName;

    private String email;

    private String password;

    private String role;

    private String registerNumber;

    private String branch;
    private String department;

    private String year;

    private String section;
}
