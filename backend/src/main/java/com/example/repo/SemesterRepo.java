
package com.example.repo;


import com.example.entity.General.Semester;
import com.example.entity.General.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SemesterRepo extends JpaRepository<Semester, Long> {

    Optional<Semester> findByYearAndTerm(int year, Term term);

}
