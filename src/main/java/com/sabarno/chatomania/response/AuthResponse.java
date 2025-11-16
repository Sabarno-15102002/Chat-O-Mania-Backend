package com.sabarno.chatomania.response;

import lombok.Data;

@Data
public class AuthResponse {

    private String token;
    private Boolean isAuth;
}
