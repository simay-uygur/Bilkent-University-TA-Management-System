
package com.example.mapper;

import com.example.dto.DepartmentStaffDto;
import com.example.entity.Actors.DepartmentStaff;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentStaffMapper {

    private final DepartmentMapper departmentMapper;

    public DepartmentStaffDto toDto(DepartmentStaff staff) {
        DepartmentStaffDto dto = new DepartmentStaffDto();
        dto.setId(staff.getId());
        dto.setName(staff.getName());
        dto.setSurname(staff.getSurname());
        dto.setIsActive(staff.getIsActive());

        if (staff.getDepartment() != null) {
            dto.setDepartmentName(staff.getDepartment().getName());
        }
        /* if (staff.getDepartment() != null) {
            dto.setDepartmentName((departmentMapper.toDto(staff.getDepartment())).getCode());
        } */

        return dto;
    }
}
