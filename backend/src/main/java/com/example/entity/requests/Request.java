package com.example.entity.Requests;

import com.example.entity.Actors.User;
import com.example.entity.General.Date;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class, 
  property = "requestId"

)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Leave.class, name = "Leave"),
})

public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "requestId", unique = true)
    private Long requestId ;

    @Column(name = "requestType", unique = false)
    private RequestType requestType;

    @Column(name = "isApproved", unique = false)
    private boolean isApproved = false;

    @Column(name = "sentTime", unique = false)
    private Date sentTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    //@JsonBackReference
      @JsonIgnoreProperties("sended_requests") // Break recursion
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    @JsonIgnoreProperties("received_requests") // Break recursion
    //@JsonBackReference
    private User receiver;

    @Column(name = "description", unique = false, nullable = true)
    private String description;
}
