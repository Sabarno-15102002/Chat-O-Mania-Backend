package com.sabarno.chatomania.service;

import java.util.List;
import java.util.UUID;

import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.request.UpdateUserRequest;

public interface UserService {

    public User findUserById(UUID id) throws UserException;
    public User findUserByEmail(String email);
    public User findUserProfile(String token) throws UserException;
    public User updateUser(UUID id, UpdateUserRequest request) throws UserException;
    public List<User> searchUser(String query);
    public User createUser(User user);
    public User createOAuthUser(String email, String name, String profilePicture);
}
