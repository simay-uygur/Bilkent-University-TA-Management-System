package com.example.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Notification;

@Repository
public interface NotificationRepos extends JpaRepository<Notification, Long> {

    // Find unread notifications for a specific user, sorted by timestamp (latest first)
    List<Notification> findByReceiverNameAndIsReadOrderByTimestampDesc(String receiverName, boolean isRead);

    // Find flagged notifications for a specific user, sorted by timestamp (latest first)
    List<Notification> findByReceiverNameAndIsFlaggedOrderByTimestampDesc(String receiverName, boolean isFlagged);

    // Find all notifications for a specific user, sorted by timestamp
    List<Notification> findByReceiverNameOrderByTimestampDesc(String receiverName);

}