package com.example.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Actors.User;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long>{
    Optional<User> findUserByName(String name);
}
