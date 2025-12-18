package com.sabarno.chatomania.utility;

import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.sabarno.chatomania.exception.BadRequestException;

public final class MediaTypeResolver {

    private static final Set<String> IMAGE_MIME = Set.of(
        "image/jpeg", "image/png", "image/webp", "image/gif", "image/heic"
    );

    private static final Set<String> VIDEO_MIME = Set.of(
        "video/mp4", "video/webm", "video/quicktime", "video/mpeg", "video/x-matroska"
    );

    private static final Set<String> AUDIO_MIME = Set.of(
        "audio/mpeg", "audio/wav", "audio/ogg", "audio/aac"
    );

    private static final Set<String> DOCUMENT_MIME = Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/zip",
        "text/plain",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/x-rar-compressed"
    );

    private MediaTypeResolver() {}

    public static MessageType resolve(MultipartFile file) throws BadRequestException {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String mime = file.getContentType();

        if (mime == null) {
            throw new BadRequestException("Unknown file type");
        }

        if (IMAGE_MIME.contains(mime)) return MessageType.IMAGE;
        if (VIDEO_MIME.contains(mime)) return MessageType.VIDEO;
        if (AUDIO_MIME.contains(mime)) return MessageType.AUDIO;
        if (DOCUMENT_MIME.contains(mime)) return MessageType.DOCUMENT;

        throw new BadRequestException("Unsupported media type");
    }
}
