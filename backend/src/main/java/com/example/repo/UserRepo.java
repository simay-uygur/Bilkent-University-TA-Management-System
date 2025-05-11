package com.example.repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Actors.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long>{
    Optional<User> findUserByName(String name);

    Optional<User> findUserByWebmail(String webmail);

}
