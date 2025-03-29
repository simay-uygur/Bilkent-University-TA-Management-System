package com.example.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@DynamicUpdate
@Getter
@Setter
@Table(name = "TA")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class TA extends User{
    
    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE} // many to many oldukca ornek bir ta silindi ondo task da silinir ama bu task 
    )
    @JoinTable( // creates a table for many to many relationship
        name = "ta_public_tasks",
        joinColumns = @JoinColumn(name = "ta_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id") // creates in ta table field for tasks 
    )
    private Set<PublicTask> ta_public_tasks_list = new HashSet<PublicTask>(); 

    @OneToMany(
        mappedBy = "ta_owner", // the other side of the relationship is the owner of the relationship
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE} // many to many oldukca ornek bir ta silindi ondo task da silinir ama bu task 
    )
    private Set<PrivateTask> ta_private_tasks_list = new HashSet<PrivateTask>(); 

    @Column(name = "academic_level", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private TAAcademicLevelType academic_level ; 
}
/*{
    "role" : "TA",
    "id" : 1, 
    "password" : "1", 
    "name" : "ta",
    "surname" : "1",
    "webmail" : "ta.1@ug.bilkent.edu.tr",
    "type" : "TA",
    "academic_level" : "MS"
} */