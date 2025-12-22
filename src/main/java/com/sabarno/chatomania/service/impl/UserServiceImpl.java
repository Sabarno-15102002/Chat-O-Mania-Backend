package com.sabarno.chatomania.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.sabarno.chatomania.config.JwtProvider;
import com.sabarno.chatomania.entity.User;
import com.sabarno.chatomania.exception.UserException;
import com.sabarno.chatomania.repository.UserRepository;
import com.sabarno.chatomania.request.UpdateUserRequest;
import com.sabarno.chatomania.service.UserService;
import com.sabarno.chatomania.utility.AuthProvider;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Override
    public User findUserById(UUID id) throws UserException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserException("User Not Found with id:" + id));
        return user;
    }

    @Override
    public User findUserProfile(String token) throws UserException {
        String email = jwtProvider.getEmailFromJwtToken(token);
        if(email == null){
            throw new BadCredentialsException("Received invalid token");
        }
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new UserException("User Not Found with email:" + email);
        }
        return user;
    }

    @Override
    public User updateUser(UUID id, UpdateUserRequest request) throws UserException {
        User user = findUserById(id);
        user.setName(request.getName() != null ? request.getName() : user.getName());
        user.setProfilePicture(request.getProfilePicture() != null ? request.getProfilePicture() : user.getProfilePicture());
        User updatedUser = userRepository.save(user);
        return updatedUser;
    }

    @Override
    public List<User> searchUser(String query) {
        return userRepository.searchUser(query);
    }

    @Override
    public User findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User createOAuthUser(String email, String name, String profilePicture) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProfilePicture(profilePicture);
        user.setAuthProvider(AuthProvider.GOOGLE);
        return userRepository.save(user);
    }

    @Override
    public User updateLastSeen(UUID userId) throws UserException{
        User user = findUserById(userId);
        user.setLastSeen(LocalDateTime.now());
        return userRepository.save(user);
    }
}
