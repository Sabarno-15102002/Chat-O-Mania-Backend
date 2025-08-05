package com.sabarno.Chat_O_Mania.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateGroupDto {
    /**
     * Unique identifier for the group chat.
     */
    private String chatName;

    /**
     * Description of the group chat.
     */
    private String chatDescription;

    /**
     *  List of user IDs to be added to the group chat.
     *  This field is a many-to-many relationship with the User entity.
     */
    private List<UUID> userIds;
}
