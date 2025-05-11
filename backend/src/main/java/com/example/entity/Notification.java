package com.example.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.entity.Actors.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "sender_id", nullable = true)
    private Long senderId; // Name of the user receiving the notification

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "text", nullable = true, columnDefinition = "TEXT")
    private String text; // Detailed text of the notification

    @Column(name = "receiver_id", nullable = true)
    private Long receiverId; // Name of the user receiving the notification

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "timestamp", nullable = true)
    private LocalDateTime timestamp; // When the notification was created

    // Constructor
    public Notification(String text, Long receiverId, LocalDateTime timestamp) {
        this.text = text;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
    }

    // toString
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", receiverName='" + receiverId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @ManyToMany(mappedBy = "notifications")
    private List<User> recipients = new ArrayList<>();
}