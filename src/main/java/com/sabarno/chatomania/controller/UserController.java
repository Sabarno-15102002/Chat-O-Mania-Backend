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

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String token) throws UserException {
        User user = userService.findUserProfile(token);
        return ResponseEntity.accepted().body(user);
    }


    @GetMapping("/{query}")
    public ResponseEntity<List<User>> searchUsers(@PathVariable("query") String query) {
        List<User> users = userService.searchUser(query);
        return ResponseEntity.ok().body(users);
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestHeader("Authorization") String token, @RequestBody UpdateUserRequest request) throws UserException {
        User user = userService.findUserProfile(token);
        User updatedUser = userService.updateUser(user.getId(), request);
        return ResponseEntity.ok().body(updatedUser);
    }
}
