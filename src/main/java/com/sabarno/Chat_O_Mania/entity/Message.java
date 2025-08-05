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

  /**
   * Unique identifier for the message.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /**
   * The user who sent the message.
   */
  @ManyToOne
  private User sender;

  /**
   * The content of the message.
   */
  private String content;

  /**
   * The timestamp when the message was sent.
   */
  @Column(name = "media_url")
  private String mediaUrl;

  /**
   * The type of media (e.g., image, video, audio).
   */
  @Column(name = "media_type")
  private String mediaType;

  /**
   * The public ID of the media, used for storage and retrieval.
   */
  @Column(name = "media_public_id")
  private String mediaPublicId;

  /**
   * The chat to which the message belongs.
   */
  @ManyToOne
  private Chats chat;

  /**
   * Indicates whether the message is edited.
   */
  @Column(name = "is_edited")
  private boolean isEdited = false;

  /**
   * Indicates whether the message is deleted.
   */
  @Column(name = "is_deleted")
  private boolean isDeleted = false;
}
