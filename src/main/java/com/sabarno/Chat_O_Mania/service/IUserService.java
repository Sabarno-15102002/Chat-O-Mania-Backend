package com.sabarno.Chat_O_Mania.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.sabarno.Chat_O_Mania.dto.LoginResponseDto;
import com.sabarno.Chat_O_Mania.dto.ProfileDto;
import com.sabarno.Chat_O_Mania.dto.RegisterRequestDto;
import com.sabarno.Chat_O_Mania.dto.UpdatePasswordRequestDto;
import com.sabarno.Chat_O_Mania.dto.UpdateUserDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.User;

public interface IUserService {

    List<User> getAllUsers();
    UserDto registerUser(RegisterRequestDto user);
    LoginResponseDto loginUser(String email, String password);
    UserDto updateUser(UpdateUserDto user, UUID userId);
    boolean updatePassword(UUID userId, UpdatePasswordRequestDto requestDto);
    Instant getLastSeen(UUID id);
    void updateProfilePicture(UUID userId, MultipartFile file);
    ProfileDto getUserProfile(UUID userId);
    void addBio(UUID userId, String bio);
    List<UserDto> getFriends(UUID userId);
    List<UserDto> searchUsers(String query, UUID userId);
    boolean removeFriend(UUID userId, UUID friendId);
}
