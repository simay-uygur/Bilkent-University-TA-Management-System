
package com.example.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private Long studentId;
    private String studentName;
    private String studentSurname;
    private String webmail;
    private String academicStatus;
    private String department;
    private Boolean isActive;
    private Boolean isGraduated;

    public StudentDto(Long studentId, String studentName, String studentSurname) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentSurname = studentSurname;
    }
}