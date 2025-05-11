package com.example.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Actors.User;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken,Long> {
  Optional<PasswordResetToken> findByToken(String token);
  void deleteByUser(User user);
}