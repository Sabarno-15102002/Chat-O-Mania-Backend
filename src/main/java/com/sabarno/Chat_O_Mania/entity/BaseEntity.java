package com.sabarno.Chat_O_Mania.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

  /**
   * The timestamp when the entity was created.
   * This field is automatically populated by the auditing system.
   */
  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  /**
   * The user who created the entity.
   * This field is automatically populated by the auditing system.
   */
  @CreatedBy
  @Column(updatable = false)
  private String createdBy;

  /**
   * The timestamp when the entity was last updated.
   * This field is automatically populated by the auditing system.
   */
  @LastModifiedDate
  @Column(insertable = false)
  private LocalDateTime updatedAt;

  /**
   * The user who last updated the entity.
   * This field is automatically populated by the auditing system.
   */
  @LastModifiedBy
  @Column(insertable = false)
  private String updatedBy;
}
