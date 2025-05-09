// com/example/dto/FacultyCourseOfferingsDto.java
package com.example.dto;

import java.util.List;

public class FacultyCourseOfferingsDto {
    private List<CourseOfferingDto> offerings;
    private List<CourseDto> courses;

    public FacultyCourseOfferingsDto() {}
    public FacultyCourseOfferingsDto(List<CourseOfferingDto> offerings,
                                     List<CourseDto> courses) {
        this.offerings = offerings;
        this.courses   = courses;
    }

    public List<CourseOfferingDto> getOfferings() { return offerings; }
    public void setOfferings(List<CourseOfferingDto> o) { offerings = o; }

    public List<CourseDto> getCourses() { return courses; }
    public void setCourses(List<CourseDto> c) { courses = c; }


}