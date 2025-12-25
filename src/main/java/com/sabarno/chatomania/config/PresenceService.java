package com.sabarno.chatomania.config;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PresenceService {

    @Cacheable(value = "presence", key = "'presence:' + #userId")
    public boolean isOnline(UUID userId) {
        return false;
    }

    @CachePut(value = "presence", key = "'presence:' + #userId")
    public boolean markOnline(UUID userId) {
        return true;
    }

    @CachePut(value = "presence", key = "'presence:' + #userId")
    public boolean markOffline(UUID userId) {
        return false;
    }

    @CacheEvict(value = "presence", key = "'presence:' + #userId")
    public void clear(UUID userId) {}
}
