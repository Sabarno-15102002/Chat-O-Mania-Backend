package com.sabarno.Chat_O_Mania.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

  /**
   * Unique identifier for the user.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "user_id", nullable = false, unique = true)
  private UUID id;

  /**
   * The username of the user.
   * This field is unique and cannot be null.
   */
  private String username;

  /**
   * The email address of the user.
   * This field is unique and cannot be null.
   */
  @Column(nullable = false, unique = true)
  private String email;

  /**
   * The password of the user.
   * This field is not nullable.
   */
  @Column(nullable = false)
  private String password;

  /**
   * The mobile number of the user.
   * This field is not nullable.
   */
  @Column(nullable = false, name = "mobile_number")
  private String mobileNumber;

  /**
   * Indicates whether the user is an admin.
   * This field is not nullable and defaults to false.
   */
  @Column(nullable = false, name = "is_admin")
  private Boolean isAdmin = false;

  /**
   * The timestamp when the user was last seen.
   * This field is nullable.
   */
  @Column(name = "last_seen")
  private Instant lastSeen;

  /**
   * The URL of the user's profile picture.
   * This field is nullable.
   */
  @Column(name = "profile_pic_url")
  private String profilePicUrl;

  /**
   * The public ID of the user's profile picture.
   * This field is nullable.
   */
  @Column(name = "profile_pic_public_id")
  private String profilePicPublicId;

  /**
   * The bio of the user.
   * This field is nullable and has a default value.
   */
  @Column(name = "bio", length = 255)
  private String bio = "Hey there! I am using Chat-O-Mania.";

  /**
   * The set of friends associated with the user.
   * This field is a many-to-many relationship with the User entity.
   */
  @ManyToMany
  @JoinTable(name = "user_friends", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "friend_id"))
  private Set<User> friends = new HashSet<>();
}
