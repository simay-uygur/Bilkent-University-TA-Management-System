package com.example.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter
@Setter
@Table(name = "TA")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class TA extends User{
        
    @Column(name = "is_deleted", unique = false, updatable = true, nullable = false)
    private boolean isDeleted = false ; 
    // dedik seyini sonra konusuruz, ama bence onceden biz ta lar ekleriz 
    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE} // many to many oldukca ornek bir ta silindi ondo task da silinir ama bu task 
    )
    @JoinTable( // creates a table for many to many relationship
        name = "ta_tasks",
        joinColumns = @JoinColumn(name = "ta_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id") // creates in ta table field for tasks 
    )
    private Set<Task> ta_tasks_list = new HashSet<Task>(); 

    private void mark_deleted() {
        this.isDeleted = true;
        this.ta_tasks_list.clear();
    }

    public void delete(){
        mark_deleted();
    }
}
