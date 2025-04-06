package com.krino.homework_8.api.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}