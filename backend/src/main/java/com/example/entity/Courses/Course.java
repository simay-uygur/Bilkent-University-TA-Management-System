package com.example.entity.Courses;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.DynamicUpdate;

import com.example.entity.General.AcademicLevelType;
import com.example.entity.General.Event;
import com.example.entity.General.Student;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Table(name = "course")
@DynamicUpdate // this is used to update only the changed fields in the database, not the whole object
public class Course {
    @Id
    @Column(name = "course_id", unique = true, updatable = true)
    private int course_id ; 
    
    @Transient
    private String course_code ; 

    

    // cs-319. id -> 'c' + 's' + 319 -> 319319
    @PrePersist
    private void setCourseId() {
        if (this.course_code != null)
            this.course_id = code_to_id(this.course_code);
    }

    public int code_to_id(String to_convert){
        if (to_convert != null){
            String[] parts = this.course_code.split("-");
            String prefix = parts[0]; // 'cs'
            String suffix = parts[1]; // '319'
            int prefix_number = prefix_to_int(prefix); 
            int suffix_number = Integer.parseInt(suffix); // 319
            if (prefix_number < 0 || prefix_number > 26) {
                throw new IllegalArgumentException("Invalid prefix: " + prefix);
            }
            String id = prefix_number + "" + suffix_number; // 'c' + 's' + 319
            return Integer.parseInt(id); // cs-319 -> 319319
        }
        return 0; // default value if course_code is null
    }

    private int prefix_to_int(String prefix){
        String to_return = "" ;
        for(int i = 0; i < prefix.length(); i++){
            int c = prefix.charAt(i) - 'a' + 1; // 'c' -> 3, 's' -> 19
            if (c < 0 || c > 26) {
                throw new IllegalArgumentException("Invalid prefix character: " + prefix.charAt(i));
            }
            to_return += c; // 'c' + 's' -> 319
        }
        return Integer.parseInt(to_return) ; 
    }

    @NotEmpty()
    @Column(name = "course_academic_status", unique = true, updatable = true, nullable = false)
    private AcademicLevelType course_academic_status ;

    @NotEmpty(message = "The field can not be empty!")
    @Column(name = "course_dep", unique = true, updatable = true)
    private String course_dep ;

    @Embedded
    private List<Event> exams ;

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinTable( // creates a table for many to many relationship
        name = "students_list_table",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<Student> students_list = new HashSet<>();

    @Column(name = "prereq_list", unique = false, updatable = true, nullable = false)
    @NotEmpty(message = "The field can not be empty!")
    private List<String> prereq_list;
    // do not use join table

    @OneToMany(
        mappedBy = "course", // the other side of the relationship is the owner of the relationship
        fetch = FetchType.LAZY
    )
    private List<Section> sections_list ; // this is the list of sections that are related to the course
}
// only one prepersist call method