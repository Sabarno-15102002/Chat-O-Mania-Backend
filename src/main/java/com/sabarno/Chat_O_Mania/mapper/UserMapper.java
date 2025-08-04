package com.sabarno.Chat_O_Mania.mapper;

import com.sabarno.Chat_O_Mania.dto.LoginResponseDto;
import com.sabarno.Chat_O_Mania.dto.ProfileDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.User;

public class UserMapper {

  public static UserDto mapToUserDto(User user, UserDto userDto) {
    userDto.setUserId(user.getId());
    userDto.setUsername(user.getUsername());
    userDto.setEmail(user.getEmail());
    userDto.setMobileNumber(user.getMobileNumber());
    userDto.setIsAdmin(user.getIsAdmin());

    return userDto;
  }

  public static ProfileDto mapToProfileDto(User user, ProfileDto profileDto) {
    profileDto.setUserId(user.getId());
    profileDto.setUsername(user.getUsername());
    profileDto.setEmail(user.getEmail());
    profileDto.setMobileNumber(user.getMobileNumber());
    profileDto.setLastSeen(user.getLastSeen());
    profileDto.setProfilePicUrl(user.getProfilePicUrl());
    profileDto.setProfilePicPublicId(user.getProfilePicPublicId());
    profileDto.setBio(user.getBio());

    return profileDto;
  }

  public static LoginResponseDto mapToLoginResponseDto(User user, LoginResponseDto responseDto , String token) {
    responseDto.setUserId(user.getId());
    responseDto.setUsername(user.getUsername());
    responseDto.setEmail(user.getEmail());
    responseDto.setMobileNumber(user.getMobileNumber());
    responseDto.setIsAdmin(user.getIsAdmin());
    responseDto.setToken(token);

    return responseDto;
  }

}
