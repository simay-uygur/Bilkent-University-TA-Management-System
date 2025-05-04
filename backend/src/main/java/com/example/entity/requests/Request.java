package com.example.entity.Requests;

import com.example.entity.Actors.User;
import com.example.entity.General.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS) // if it is not included it will add every user with different roles to the same table in mysql. table per class means for each class(ta,deans office) there is their own table 
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Leave.class, name = "Leave"),
    @JsonSubTypes.Type(value = ProctorTaInFaculty.class, name = "ProctorTaInFaculty"),
    @JsonSubTypes.Type(value = ProctorTaFromFaculties.class, name = "ProctorTaFromFaculties"),
    @JsonSubTypes.Type(value = Swap.class, name = "Swap"),
    @JsonSubTypes.Type(value = SwapEnable.class, name = "SwapEnable"),
    @JsonSubTypes.Type(value = WorkLoad.class, name = "Workload"),
    @JsonSubTypes.Type(value = TransferProctoring.class, name = "TransferProctoring")
})

public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "request_id", unique = true)
    private Long requestId ;

    @Column(name = "request_type", unique = false)
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(name = "is_approved", unique = false)
    private boolean isApproved = false;

    @Column(name = "is_rejected", unique = false)
    private boolean isRejected = false;

    @Column(name = "is_pending", unique = false)
    private boolean isPending = true;

    @Column(name = "sent_time", unique = false)
    private Date sentTime;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(name = "description", unique = false, nullable = true)
    private String description;
}
