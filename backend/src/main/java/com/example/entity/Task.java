package com.example.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PublicTask.class, name = "PUBLIC"),
    @JsonSubTypes.Type(value = PrivateTask.class, name = "PRIVATE")
})
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE) // auto id generation
    @Column(name = "task_id", unique = true, updatable = false, nullable = false)
    private int task_id;

    /*@ManyToOne // when using relationships needs to know who 
    @JoinColumn(name = "course_id")
    private Course course; //many to one*/

    /*@ManyToMany(mappedBy="section_tasks_list",fetch = FetchType.LAZY)
    private List<Section> sections_list ; //many to many*/
    
    @Embedded
    @Column(name = "duration", unique = false, updatable = true, nullable = false)
    private Event duration;

    @Column(name = "is_time_passed", unique = false, updatable = true, nullable = false)
    private boolean isTimePassed;

    @Column(name = "workload", unique = false, updatable = true, nullable = false)
    private int workload; 

    @Column(name = "task_type", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskType task_type;
    
    @Column(name = "status", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskState status = TaskState.UNKNOWN;

    /*@Column(name = "access_type", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskAccessType accessType ;*/

    //Method to check if task is still active
    public boolean isTaskActive() {
        return duration != null && duration.isOngoing();
    }
}
