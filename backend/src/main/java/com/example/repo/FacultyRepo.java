package com.example.repo;



import com.example.entity.General.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepo extends JpaRepository<Faculty, String> {

}
