package com.sabarno.chatomania.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabarno.chatomania.entity.Media;

public interface MediaRepository extends JpaRepository<Media, UUID>{

}
