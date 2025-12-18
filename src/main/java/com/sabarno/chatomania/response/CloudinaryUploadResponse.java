package com.sabarno.chatomania.response;

import com.sabarno.chatomania.utility.MessageType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CloudinaryUploadResponse {
    private String url;
    private String publicId;
    private String resourceType;
    private Long size;
    private MessageType messageType;
    private String thumbnail;
    private Long duration;
}

