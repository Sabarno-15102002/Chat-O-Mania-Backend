package com.sabarno.chatomania.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabarno.chatomania.config.JwtProvider;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.response.AuthResponse;
import com.sabarno.chatomania.service.CustomUserService;
import com.sabarno.chatomania.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "APIs for user registration and login")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    private CustomUserService customUserDetailsService;


    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token upon successful registration")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody User user) throws UserException{
        String email = user.getEmail();
        String name = user.getName();
        String password = user.getPassword();

        User existingUser = userService.findUserByEmail(email);
        if(existingUser != null){
            throw new UserException("User already exists with email: " + email);
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setPassword(passwordEncoder.encode(password));
        userService.createUser(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);

        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setIsAuth(true);

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @Operation(summary = "Login a user", description = "Authenticates a user and returns a JWT token upon successful login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUserHandler(@RequestBody User user) throws UserException{
        String email = user.getEmail();
        String password = user.getPassword();

        Authentication authentication = authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);

        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setIsAuth(true);

        return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
    }

    private Authentication authenticate(String email, String password) {

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid email");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
