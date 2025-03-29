package com.example.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "public_task")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PublicTask extends Task{

    @Column(name = "required_tas", unique = false, updatable = true, nullable = false)
    private int requiredTAs;

    @Column(name = "size_of_tas", unique = false, updatable = true, nullable = false)
    private int amount_of_tas = 0;

    @ManyToMany(
        mappedBy="ta_public_tasks_list",
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    ) // mappedby means that the other side is the owner of the relationship(TA)
    @JsonIgnore
    private Set<TA> tas_list = new HashSet<TA>(); //many to many

    public boolean addTA(){
        if (amount_of_tas < requiredTAs) {
            amount_of_tas++;
            return true;
        }
        return false;
    }

    public boolean removeTA(){
        if (amount_of_tas > 0) {
            amount_of_tas--;
            return true;
        }
        return false;
    }
}
