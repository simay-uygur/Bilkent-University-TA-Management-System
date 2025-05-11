package com.example.entity.Requests;

import com.example.entity.General.Date;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
    @JsonSubTypes.Type(value = ProctorTaInDepartment.class, name = "ProctorTaInDepartment"),
    @JsonSubTypes.Type(value = ProctorTaFromFaculties.class, name = "ProctorTaFromFaculties"),
    @JsonSubTypes.Type(value = Swap.class, name = "Swap"),
    @JsonSubTypes.Type(value = WorkLoad.class, name = "Workload"),
    @JsonSubTypes.Type(value = TransferProctoring.class, name = "TransferProctoring"),
    @JsonSubTypes.Type(value = ProctorTaFromOtherFaculty.class, name = "ProctorTaFromFaculty"),
    @JsonSubTypes.Type(value = PreferTasToCourse.class, name = "PreferTasToCourse")
})

public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "request_id", unique = true)
    private Long requestId ;

    @Column(name = "request_type", unique = false, length = 50)
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

    @Column(name = "course_code")
    private String courseCode;

    @Column(name = "description", unique = false, nullable = true)
    private String description;
}
