package com.example.entity.Notifications;

import com.example.entity.Actors.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bildirimin muhatabının ID'si
     * (örneğin TA ise TA.id, DEANS_OFFICE ise ilgili deanOffice kaydının ID'si)
     */
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    /**
     * Bildirimin muhatabı olarak hangi Role
     * (TA, FACULTY_MEMBER, DEANS_OFFICE, DEPARTMENT_STAFF vs.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_role", nullable = false)
    private Role recipientRole;

    /** Başlık, örn. "Yeni TA Görevi" */
    @Column(nullable = false)
    private String title;

    /** Bildirim metni / içeriği */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Okunma durumu */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    /** Oluşturulma zamanı */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    // ---------------------------------------
    // Constructors
    // ---------------------------------------

    public Notification() {
        // JPA için no‐arg constructor
    }

    public Notification(Long recipientId,
                        Role recipientRole,
                        String title,
                        String message) {
        this.recipientId   = recipientId;
        this.recipientRole = recipientRole;
        this.title         = title;
        this.message       = message;
        this.read          = false;
        this.createdAt     = LocalDateTime.now();
    }


    // ---------------------------------------
    // Getters & Setters
    // ---------------------------------------

    public Long getId() {
        return id;
    }

    public Long getRecipientId() {
        return recipientId;
    }
    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Role getRecipientRole() {
        return recipientRole;
    }
    public void setRecipientRole(Role recipientRole) {
        this.recipientRole = recipientRole;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    // Eğer dışarıdan değiştirmek istemezsen bu setter'ı kaldırabilirsin
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    // ---------------------------------------
    // toString (opsiyonel)
    // ---------------------------------------

    @Override
    public String toString() {
        return "Notification{" +
               "id=" + id +
               ", recipientId=" + recipientId +
               ", recipientRole=" + recipientRole +
               ", title='" + title + '\'' +
               ", message='" + message + '\'' +
               ", read=" + read +
               ", createdAt=" + createdAt +
               '}';
    }
}
