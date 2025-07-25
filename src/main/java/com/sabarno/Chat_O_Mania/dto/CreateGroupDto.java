package com.sabarno.Chat_O_Mania.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateGroupDto {
    private String chatName;
    private String chatDescription;
    private List<UUID> userIds;
}
