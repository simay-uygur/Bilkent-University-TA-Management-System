package com.example.entity.Actors;



import java.util.ArrayList;
import java.util.List;

import com.example.entity.General.Faculty;
import com.example.entity.Requests.ProctorTaFromFaculties;
import com.example.entity.Requests.ProctorTaFromOtherFaculty;
import com.example.entity.Requests.ProctorTaInFaculty;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dean_office_table")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DeanOffice extends User {


    @OneToOne(mappedBy = "deanOffice", fetch = FetchType.LAZY)
    private Faculty faculty;

    /** Convenience ctor to set role automatically. */
    /*public DeanOffice(Long id,
                      String password,
                      String name,
                      String surname,
                      String webmail) {
        super(id, password, name, surname, webmail, Role.DEANS_OFFICE, false);
    }*/

    @OneToMany(mappedBy = "sender",fetch= FetchType.LAZY, cascade= CascadeType.ALL)
    private List<ProctorTaFromFaculties> sendedFromFacsRequests = new ArrayList<>();

    @OneToMany(mappedBy = "sender",fetch= FetchType.LAZY, cascade= CascadeType.ALL)
    private List<ProctorTaFromOtherFaculty> sendedFromOtherFacRequests = new ArrayList<>();

    @OneToMany(mappedBy = "receiver",fetch= FetchType.LAZY, cascade= CascadeType.ALL)
    private List<ProctorTaFromOtherFaculty> receivedFromFacsRequests = new ArrayList<>();

    @OneToMany(
        mappedBy = "receiver",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ProctorTaInFaculty> receivedFromDepartmentsRequests = new ArrayList<>();
}