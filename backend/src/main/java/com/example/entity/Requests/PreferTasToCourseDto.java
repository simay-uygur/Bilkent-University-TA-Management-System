package com.example.entity.Requests;

import java.util.List;

import com.example.dto.RequestDto;
import com.example.entity.General.Date;

import lombok.Data;

@Data
public class PreferTasToCourseDto extends RequestDto{
    private Long requestId;

    // Department that receives the request
    private String departmentName;

    // Instructor who sent the request
    private Long instructorId;
    private String instructorName;
    private String instructorSurname;

    // Course for which TAs are preferred
    private Long courseId;

    // Section for which the request applies
    private Long sectionId;
    private String sectionCode;

    // Number of TAs needed
    private int taNeeded;

    // Number of already assigned TAs
    private int amountOfAssignedTas = 0;

    // Preferred TAs
    private List<TaInfo> preferredTas;

    // Non-preferred TAs
    private List<TaInfo> nonPreferredTas;

    private Date sentTime;

    private RequestType requesType;
    @Data
    public static class TaInfo {
        private Long id;
        private String name;
        private String surname;
    }
}
