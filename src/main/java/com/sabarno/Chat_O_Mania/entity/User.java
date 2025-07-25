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

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "user_id", nullable = false, unique = true)
  private UUID id;
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, name = "mobile_number")
  private String mobileNumber;

  @Column(nullable = false, name = "is_admin")
  private Boolean isAdmin = false;

  @Column(name = "last_seen")
  private Instant lastSeen;

  @Column(name = "profile_pic_url")
  private String profilePicUrl;

  @Column(name = "profile_pic_public_id")
  private String profilePicPublicId;

  @Column(name = "bio", length = 255)
  private String bio = "Hey there! I am using Chat-O-Mania.";

  @ManyToMany
  @JoinTable(name = "user_friends", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "friend_id"))
  private Set<User> friends = new HashSet<>();
}
