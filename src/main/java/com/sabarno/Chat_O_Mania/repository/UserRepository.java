package com.sabarno.Chat_O_Mania.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sabarno.Chat_O_Mania.entity.User;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.lastSeen = :time WHERE u.id = :id")
  void updateLastSeen(@Param("id") UUID id, @Param("time") Instant time);

  List<User> findByUsernameContainingIgnoreCase(String query);

}
