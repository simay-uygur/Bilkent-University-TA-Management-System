package com.example.entity.Curriculum;

import com.example.entity.Actors.TA;
import com.example.entity.Courses.Section;
import com.example.entity.General.Event;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "daily_work")
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE) // auto id generation
    @Column(name = "duty_id", unique = true, updatable = false, nullable = false)
    // this is the id of the duty, not the task id
    private int duty_id;
    
    @Column(name = "duty_type", unique = false, updatable = true, nullable = false)
    private DutyType duty_type;

    @Embedded
    @Column(name = "duration" , unique = false, updatable = true, nullable = false)
    private Event duration; 

    //what is many to one?
    // many to one is used to specify the relationship between the two entities. In this case, the TA class is the owner of the relationship.
    @ManyToOne(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private TA ta;

    @OneToOne(
        mappedBy = "duty", // the other side of the relationship is the owner of the relationship
        // this is the section that the duty is related to, not the course
        fetch = FetchType.LAZY,// lazy loading is used to avoid loading the whole section when only the duty is needed
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
        // if the section is deleted, the duty is also deleted, but if the duty is deleted, the section is not deleted
    )
    private Section section; // section is the course that the duty is related to
}
