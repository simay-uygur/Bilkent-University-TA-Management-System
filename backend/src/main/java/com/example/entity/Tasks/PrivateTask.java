package com.example.entity.Tasks;

import com.example.entity.Actors.TA;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "private_tasks_table")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PrivateTask extends Task{
    
    @ManyToOne(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinColumn(name = "ta_id")
    @JsonIgnore
    private TA ta_owner;
}
