package com.sabarno.chatomania.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.sabarno.chatomania.config.JwtProvider;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.response.AuthResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String profilePicture = oauthUser.getAttribute("picture");

        User user = userService.findUserByEmail(email);
        if (user == null) {
            user = userService.createOAuthUser(email, name, profilePicture);
        }

        String jwt = jwtProvider.generateTokenForOAuth(user.getEmail());

        ResponseEntity<AuthResponse> authResponse = ResponseEntity.ok(new AuthResponse(jwt, true));

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(authResponse.getBody().toString());
    }
}

