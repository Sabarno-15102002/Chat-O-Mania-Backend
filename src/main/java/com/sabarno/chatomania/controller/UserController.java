package com.sabarno.chatomania.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.request.UpdateUserRequest;
import com.sabarno.chatomania.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "APIs for managing user profiles and searching users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get user profile", description = "Retrieves the profile of the authenticated user")
    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String token) throws UserException {
        User user = userService.findUserProfile(token);
        return ResponseEntity.accepted().body(user);
    }

    @Operation(summary = "Search users", description = "Searches for users by name or email matching the query")
    @GetMapping("/{query}")
    public ResponseEntity<List<User>> searchUsers(@PathVariable("query") String query) {
        List<User> users = userService.searchUser(query);
        return ResponseEntity.ok().body(users);
    }

    @Operation(summary = "Update user profile", description = "Updates the profile information of the authenticated user")
    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestHeader("Authorization") String token, @RequestBody UpdateUserRequest request) throws UserException {
        User user = userService.findUserProfile(token);
        User updatedUser = userService.updateUser(user.getId(), request);
        return ResponseEntity.ok().body(updatedUser);
    }
}
