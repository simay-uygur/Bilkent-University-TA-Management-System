package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lightweight view of a Faculty.
 *
 * departments -- optional; include only if you really need the list
 *               in the response. Otherwise set it to null in the mapper.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDto {

    private String code;                 // “ENG”, “SCI”, …

    private String title;                // “Faculty of Engineering”

    /** Optional child list that does NOT point back to Faculty */
    private List<DepartmentDto> departments;
}