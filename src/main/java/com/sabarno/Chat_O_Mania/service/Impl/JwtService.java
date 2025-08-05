package com.sabarno.Chat_O_Mania.service.Impl;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
@Service
public class JwtService {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expiration = 86400000; // 1 day

    /**
     * Generates a JWT token for the given user ID.
     *
     * @param userId the ID of the user for whom the token is generated
     * @return a JWT token as a String
     */
    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the user ID from the given JWT token.
     *
     * @param token the JWT token from which to extract the user ID
     * @return the user ID as a String
     */
    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates the given JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            extractUserId(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}