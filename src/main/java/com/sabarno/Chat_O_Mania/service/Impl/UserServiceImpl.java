package com.sabarno.Chat_O_Mania.service.Impl;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sabarno.Chat_O_Mania.config.RateLimiter;
import com.sabarno.Chat_O_Mania.dto.LoginResponseDto;
import com.sabarno.Chat_O_Mania.dto.ProfileDto;
import com.sabarno.Chat_O_Mania.dto.RegisterRequestDto;
import com.sabarno.Chat_O_Mania.dto.UpdatePasswordRequestDto;
import com.sabarno.Chat_O_Mania.dto.UpdateUserDto;
import com.sabarno.Chat_O_Mania.dto.UserDto;
import com.sabarno.Chat_O_Mania.entity.User;
import com.sabarno.Chat_O_Mania.exception.NotValidDataException;
import com.sabarno.Chat_O_Mania.exception.ResourceNotFoundException;
import com.sabarno.Chat_O_Mania.exception.TooManyRequestsException;
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

  @Autowired
  private RedisCacheManager cacheManager;

  @Autowired
  private RateLimiter rateLimiter;

  private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

  /**
   * Retrieves all users in the system.
   *
   * @return a list of User objects representing all users
   */
  @Override
  public List<User> getAllUsers() {
    return null; // TODO: Implement this method to return all users
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
      throw new NotValidDataException("User already exists with email: " + request.getEmail());
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
    if (!rateLimiter.isAllowed("login:" + email, 5, Duration.ofMinutes(5))) {
      throw new TooManyRequestsException("Too many login attempts. Please wait and try again.");
    }
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new NotValidDataException("Invalid email or password"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new NotValidDataException("Invalid email or password");
    }

    String token = jwtService.generateToken(user.getId().toString());
    cacheManager.getCache("loginSessions").put(user.getId().toString(), token);
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
    cacheManager.getCache("userProfile").evict(userId);
    cacheManager.getCache("userProfile").put(userId, UserMapper.mapToProfileDto(updatedUser, new ProfileDto()));
    return UserMapper.mapToUserDto(updatedUser, new UserDto());
  }

  /**
   * Updates the password for a user.
   *
   * @param userId     the ID of the user whose password is to be updated
   * @param requestDto the request containing old and new passwords
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

    // Evict the user profile cache to ensure the updated password is reflected
    cacheManager.getCache("userProfile").evict(userId);
    cacheManager.getCache("userProfile").put(userId, UserMapper.mapToProfileDto(user, new ProfileDto()));
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

    Cache cache = cacheManager.getCache("userProfile");
    if (cache != null && cache.get(id) != null) {
      ProfileDto profileDto = (ProfileDto) cache.get(id).get();
      if (profileDto != null && profileDto.getLastSeen() != null) {
        return profileDto.getLastSeen();
      }
    }
    Instant lastseen = userRepository.findById(id)
        .map(User::getLastSeen)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    return lastseen != null ? lastseen : Instant.now();
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
      // Upload new picture using preset
      if (file.isEmpty()) {
        throw new NotValidDataException("File is empty");
      }

      String contentType = file.getContentType();
      if (contentType == null ||
          !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
        throw new NotValidDataException("Invalid file type. Only JPG, PNG are allowed.");
      }

      if (file.getSize() > 2 * 1024 * 1024) {
        throw new NotValidDataException("File too large. Max allowed size is 2MB.");
      }

      // Delete old profile picture if it exists
      if (user.getProfilePicPublicId() != null && !user.getProfilePicPublicId().isBlank()) {
        cloudinary.uploader().destroy(user.getProfilePicPublicId(), ObjectUtils.emptyMap());
      }

      Map<String, Object> options = ObjectUtils.asMap(
          "upload_preset", "ChatOManiaProfilePic");

      Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

      String imageUrl = uploadResult.get("secure_url").toString();
      String publicId = uploadResult.get("public_id").toString();

      user.setProfilePicUrl(imageUrl);
      user.setProfilePicPublicId(publicId);

      userRepository.save(user);
      cacheManager.getCache("userProfile").evict(userId);
      cacheManager.getCache("userProfile").put(userId, UserMapper.mapToProfileDto(user, new ProfileDto()));
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
  @Cacheable(value = "userProfile", key = "#userId")
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

    if (bio.length() > 50) {
      throw new NotValidDataException("Bio cannot exceed 50 characters");
    }

    user.setBio(bio);
    userRepository.save(user);
    cacheManager.getCache("userProfile").evict(userId);
    cacheManager.getCache("userProfile").put(userId, UserMapper.mapToProfileDto(user, new ProfileDto()));
  }

  /**
   * Retrieves a list of friends for a user.
   *
   * @param userId the ID of the user whose friends are to be retrieved
   * @return a list of UserDto objects representing the user's friends
   */
  @Override
  @Cacheable(value = "userFriends", key = "#userId")
  public List<UserDto> getFriends(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    return user.getFriends().stream()
        .filter(friend -> !user.getBlockedUsers().contains(friend) && !friend.getBlockedUsers().contains(user))
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
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    return users.stream()
        .filter(user -> !user.getId().equals(userId)) // Exclude the current user
        .filter(user -> !currentUser.getBlockedUsers().contains(user) && !user.getBlockedUsers().contains(currentUser))
        .map(user -> UserMapper.mapToUserDto(user, new UserDto()))
        .toList();
  }

  /**
   * Removes a friend from the user's friend list.
   *
   * @param userId   the ID of the user removing the friend
   * @param friendId the ID of the friend to be removed
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
    cacheManager.getCache("userFriends").evict(userId);
    cacheManager.getCache("userFriends").put(userId, friend);
    return true;
  }

  @Override
  public void blockUser(UUID userId, UUID targetUuid) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    User blockedUser = userRepository.findById(targetUuid)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    currentUser.getBlockedUsers().add(blockedUser);
    userRepository.save(currentUser);
  }

  @Override
  public void unblockUser(UUID userId, UUID blockedUserId) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    currentUser.getBlockedUsers().removeIf(u -> u.getId().equals(blockedUserId));
    userRepository.save(currentUser);
  }
}
