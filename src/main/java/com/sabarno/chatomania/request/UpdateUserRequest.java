package com.sabarno.chatomania.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String profilePicture;
}
