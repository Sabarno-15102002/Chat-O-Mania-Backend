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

  /**
   * Retrieves all users in the system.
   *
   * @return a list of User objects representing all users
   */
  @Override
  public List<User> getAllUsers() {
    return null; //TODO: Implement this method to return all users
  }

  /**
   * Registers a new user in the system.
   *
   * @param request the registration request containing user details
   * @return a UserDto object representing the registered user
   */
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

  /**
   * Logs in a user with the provided email and password.
   *
   * @param email    the email of the user
   * @param password the password of the user
   * @return a LoginResponseDto containing user details and JWT token
   */
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

  /**
   * Updates user details based on the provided UpdateUserDto.
   *
   * @param user   the UpdateUserDto containing updated user details
   * @param userId the ID of the user to update
   * @return a UserDto object representing the updated user
   */
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

  /**
   * Updates the password for a user.
   *
   * @param userId      the ID of the user whose password is to be updated
   * @param requestDto  the request containing old and new passwords
   * @return true if the password was updated successfully, false otherwise
   */
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

  /**
   * Retrieves the last seen timestamp of a user.
   *
   * @param id the ID of the user
   * @return the last seen timestamp as an Instant
   */
  @Override
  public Instant getLastSeen(UUID id) {
    return userRepository.findById(id)
        .map(User::getLastSeen)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
  }

  /**
   * Updates the profile picture of a user.
   *
   * @param userId the ID of the user whose profile picture is to be updated
   * @param file   the MultipartFile containing the new profile picture
   */
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

  /**
   * Retrieves the user profile by user ID.
   *
   * @param userId the ID of the user whose profile is to be retrieved
   * @return a ProfileDto object containing user profile details
   */
  @Override
  public ProfileDto getUserProfile(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    return UserMapper.mapToProfileDto(user, new ProfileDto());
  }

  /**
   * Adds a bio to the user's profile.
   *
   * @param userId the ID of the user
   * @param bio    the bio to be added
   */
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

  /**
   * Retrieves a list of friends for a user.
   *
   * @param userId the ID of the user whose friends are to be retrieved
   * @return a list of UserDto objects representing the user's friends
   */
  @Override
  public List<UserDto> getFriends(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    return user.getFriends().stream()
        .map(friend -> UserMapper.mapToUserDto(friend, new UserDto()))
        .toList();
  }

  /**
   * Searches for users by username.
   *
   * @param query  the search query
   * @param userId the ID of the user performing the search
   * @return a list of UserDto objects matching the search query
   */
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

  /**
   * Removes a friend from the user's friend list.
   *
   * @param userId    the ID of the user removing the friend
   * @param friendId  the ID of the friend to be removed
   * @return true if the friend was removed successfully, false otherwise
   */
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
