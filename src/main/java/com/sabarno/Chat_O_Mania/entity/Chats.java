package com.sabarno.Chat_O_Mania.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chats extends BaseEntity {

  /**
   * Unique identifier for the chat.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * Name of the chat.
   */
  @Column(name = "chat_name")
  private String chatName;

  /**
   * Indicates whether the chat is a group chat.
   */
  @Column(name = "is_group_chat", nullable = false)
  private Boolean isGroupChat = false;

  /**
   * Indicates whether the chat is a secret chat.
   */
  @ManyToMany
  @JoinTable(name = "chat_users", joinColumns = @JoinColumn(name = "chat_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  private List<User> users = new ArrayList<>();

  /**
   * List of latest messages in the chat.
   */
  @OneToMany(mappedBy = "chat")
  private List<Message> latestMessages;

  /**
   * List of group admins in the chat.
   */
  @ManyToMany
  @JoinTable(name = "chat_group_admins", joinColumns = @JoinColumn(name = "chat_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  private List<User> groupAdmins = new ArrayList<>();

  /**
   * Description of the chat.
   */
  private String chatDescription;

  @OneToMany
  @JoinTable(name = "chat_pinned_messages", joinColumns = @JoinColumn(name = "chat_id"), inverseJoinColumns = @JoinColumn(name = "message_id"))
  private Set<Message> pinnedMessages = new HashSet<>();

}
