package com.sabarno.Chat_O_Mania.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequestDto {
    private String oldPassword;
    private String newPassword;
}
