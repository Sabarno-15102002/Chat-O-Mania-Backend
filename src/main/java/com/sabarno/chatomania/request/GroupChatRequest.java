package com.sabarno.chatomania.request;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class GroupChatRequest {
    private String groupName;
    private List<UUID> userIds;
    private String groupIcon;
    private String description;
}
