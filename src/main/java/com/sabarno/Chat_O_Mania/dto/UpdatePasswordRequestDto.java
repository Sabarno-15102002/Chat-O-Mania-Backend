package com.sabarno.Chat_O_Mania.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequestDto {

    /**
     * The old password of the user.
     * This field is required for authentication before updating the password.
     */
    private String oldPassword;

    /**
     * The new password for the user.
     * This field is required to set a new password.
     */
    private String newPassword;
}
