package com.example.entity.Requests;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Department;
import com.example.entity.General.Event;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Leave extends Request{
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "attachment", columnDefinition = "MEDIUMBLOB")
    private byte[] attachment;

    @Column(name = "attachment_filename", length = 255)
    private String attachmentFilename;

    @Column(name = "attachment_content_type", length = 100)
    private String attachmentContentType;

    @Embedded
    @Column(name = "duration", unique = false, updatable = true,  nullable = true)
    private Event duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name", referencedColumnName = "name")
    Department receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ta_id", referencedColumnName = "id")
    private TA sender;
}
