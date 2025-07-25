package com.sabarno.Chat_O_Mania.entity;

import java.util.ArrayList;
import java.util.List;
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

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "chat_name")
  private String chatName;

  @Column(name = "is_group_chat", nullable = false)
  private Boolean isGroupChat = false;

  @ManyToMany
  @JoinTable(name = "chat_users", joinColumns = @JoinColumn(name = "chat_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  private List<User> users = new ArrayList<>();

  @OneToMany(mappedBy = "chat")
  private List<Message> latestMessages;

  @ManyToMany
  @JoinTable(name = "chat_group_admins", joinColumns = @JoinColumn(name = "chat_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  private List<User> groupAdmins = new ArrayList<>();

  private String chatDescription;
}
