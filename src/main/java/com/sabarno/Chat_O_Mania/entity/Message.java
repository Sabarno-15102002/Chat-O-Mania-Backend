package com.sabarno.Chat_O_Mania.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  private User sender;

  private String content;

  @Column(name = "media_url")
  private String mediaUrl;

  @Column(name = "media_type")
  private String mediaType;

  @Column(name = "media_public_id")
  private String mediaPublicId;

  @ManyToOne
  private Chats chat;

  @Column(name = "is_edited")
private boolean isEdited = false;

@Column(name = "is_deleted")
private boolean isDeleted = false;
}
