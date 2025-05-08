package com.example.entity.Actors;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.DynamicUpdate;
import com.example.entity.Courses.Department;
import com.example.entity.Courses.Section;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "instructor_table")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor //this was missing
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Instructor extends User{

    //no course of course offering direct relation from the instructor - but section

    @Column(name = "is_active", updatable = false,  nullable = false)  //added new
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "is_in_faculty")
    private Boolean isInFaculty;  // allow null

    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    private List<Section> sections = new ArrayList<>(); //newly added 

}
