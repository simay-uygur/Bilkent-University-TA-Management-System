package com.example.demo1.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS) // if it is not included it will add every user with different roles to the same table in mysql. table per class means for each class(ta,deans office) there is their own table 
@NoArgsConstructor
@AllArgsConstructor
//do not write table to user class
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class User {
    @Id
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private Long id ;
    
    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "password", unique = false, updatable = true, nullable = false)
    private String password ;
    
    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "name", unique = false, updatable = true, nullable = false)
    private String name ;

    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "surname", unique = false, updatable = true, nullable = false)
    private String surname ;

    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "webmail", unique = true, updatable = true, nullable = false)
    private String webmail ;

    @NotNull(message = "The field can not be empty!")
    @Column(name = "role", unique = false, updatable = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role ;
    
    @Column(name = "is_deleted", unique = false, updatable = true, nullable = false)
    private boolean isDeleted = false ; 
}
