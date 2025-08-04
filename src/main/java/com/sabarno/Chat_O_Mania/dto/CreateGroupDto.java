package com.sabarno.Chat_O_Mania.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateGroupDto {
    private String chatName;
    private String chatDescription;
    private List<UUID> userIds;
}
