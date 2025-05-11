package com.example.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Notification;

@Repository
public interface NotificationRepos extends JpaRepository<Notification, Long> {
    List<Notification> findAllByReceiverIdOrderByTimestampDesc(Long receiverId);
}