// com/example/dto/FacultyCourseOfferingsDto.java
package com.example.dto;

import java.util.List;

public class FacultyCourseDto {
    private List<CourseOfferingDto> offerings;
    private List<CourseDto> courses;

    public FacultyCourseDto() {}
    public FacultyCourseDto(
                                     List<CourseDto> courses) {
        this.courses   = courses;
    }

    public List<CourseDto> getCourses() { return courses; }
    public void setCourses(List<CourseDto> c) { courses = c; }


}