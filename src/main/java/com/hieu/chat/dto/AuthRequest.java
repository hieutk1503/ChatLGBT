package com.hieu.chat.dto;

import lombok.Data;

public class AuthRequest {

    @Data
    public static class Login {
        private String email;
        private String password;
    }

    @Data
    public static class Register {
        private String email;
        private String password;
        private String fullName;
    }
}