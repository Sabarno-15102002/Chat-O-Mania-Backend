package com.sabarno.Chat_O_Mania.service.Impl;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sabarno.Chat_O_Mania.dto.LoginResponseDto;
import com.sabarno.Chat_O_Mania.dto.ProfileDto;
import com.sabarno.Chat_O_Mania.dto.RegisterRequestDto;
import com.sabarno.Chat_O_Mania.dto.UpdatePasswordRequestDto;
import com.sabarno.Chat_O_Mania.dto.UpdateUserDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.User;
import com.sabarno.Chat_O_Mania.exception.NotValidDataException;
import com.sabarno.Chat_O_Mania.exception.ResourceNotFoundException;
import com.sabarno.Chat_O_Mania.mapper.UserMapper;
import com.sabarno.Chat_O_Mania.repository.UserRepository;
import com.sabarno.Chat_O_Mania.service.IUserService;

@Service
public class UserServiceImpl implements IUserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private Cloudinary cloudinary;

  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

  @Override
  public List<User> getAllUsers() {
    return null;
  }

  @Override
  public UserDto registerUser(RegisterRequestDto request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("User already exists with email: " + request.getEmail());
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setMobileNumber(request.getMobileNumber());
    user.setIsAdmin(request.getIsAdmin() != null ? request.getIsAdmin() : false);

    User savedUser = userRepository.save(user);
    return UserMapper.mapToUserDto(savedUser, new UserDto());
  }

  @Override
  public LoginResponseDto loginUser(String email, String password) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new NotValidDataException("Invalid email or password"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new NotValidDataException("Invalid email or password");
    }

    String token = jwtService.generateToken(user.getId().toString());
    return UserMapper.mapToLoginResponseDto(user, new LoginResponseDto(), token);
  }

  @Override
  public UserDto updateUser(UpdateUserDto user, UUID userId) {
    User existingUser = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getUsername() != null) {
      existingUser.setUsername(user.getUsername());
    }
    if (user.getEmail() != null) {
      existingUser.setEmail(user.getEmail());
    }
    if (user.getMobileNumber() != null) {
      existingUser.setMobileNumber(user.getMobileNumber());
    }

    User updatedUser = userRepository.save(existingUser);
    return UserMapper.mapToUserDto(updatedUser, new UserDto());
  }

  @Override
  public boolean updatePassword(UUID userId, UpdatePasswordRequestDto requestDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
      throw new NotValidDataException("Old password is incorrect.");
    }

    user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
    userRepository.save(user);

    return true;
  }

  @Override
  public Instant getLastSeen(UUID id) {
    return userRepository.findById(id)
        .map(User::getLastSeen)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
  }

  @SuppressWarnings("unchecked")
  public void updateProfilePicture(UUID userId, MultipartFile file) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    try {
      // Delete old profile picture if it exists
      if (user.getProfilePicPublicId() != null && !user.getProfilePicPublicId().isBlank()) {
        cloudinary.uploader().destroy(user.getProfilePicPublicId(), ObjectUtils.emptyMap());
      }

      // Upload new picture using preset
      if (file.isEmpty()) {
        throw new NotValidDataException("File is empty");
      }

      Map<String, Object> options = ObjectUtils.asMap(
          "upload_preset", "ChatOManiaProfilePic");

      Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

      String imageUrl = uploadResult.get("secure_url").toString();
      String publicId = uploadResult.get("public_id").toString();

      user.setProfilePicUrl(imageUrl);
      user.setProfilePicPublicId(publicId);

      userRepository.save(user);
    } catch (IOException e) {
      throw new RuntimeException("Failed to upload image", e);
    }
  }

  @Override
  public ProfileDto getUserProfile(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    return UserMapper.mapToProfileDto(user, new ProfileDto());
  }

  @Override
  public void addBio(UUID userId, String bio) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    if (bio == null || bio.isBlank()) {
      throw new NotValidDataException("Bio cannot be empty");
    }

    user.setBio(bio);
    userRepository.save(user);
  }

  @Override
  public List<UserDto> getFriends(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    return user.getFriends().stream()
        .map(friend -> UserMapper.mapToUserDto(friend, new UserDto()))
        .toList();
  }

  @Override
  public List<UserDto> searchUsers(String query, UUID userId) {
    if (query == null || query.isBlank()) {
      throw new NotValidDataException("Search query cannot be empty");
    }

    List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
    
    return users.stream()
        .filter(user -> !user.getId().equals(userId)) // Exclude the current user
        .map(user -> UserMapper.mapToUserDto(user, new UserDto()))
        .toList();
  }

  @Override
  public boolean removeFriend(UUID userId, UUID friendId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    User friend = userRepository.findById(friendId)
        .orElseThrow(() -> new ResourceNotFoundException("Friend not found with ID: " + friendId));

    if (!user.getFriends().contains(friend)) {
      throw new ResourceNotFoundException("Friend not found in user's friend list");
    }

    user.getFriends().remove(friend);
    friend.getFriends().remove(user);

    userRepository.save(user);
    userRepository.save(friend);

    return true;
  }
}
