package com.example.repo.RequestRepos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.PreferTasToCourse;

@Repository
public interface PreferTasToCourseRepo extends JpaRepository<PreferTasToCourse, Long>{
    boolean existsBySender_IdAndSection_SectionIdAndReceiver_Name(
        Long instructorId,
        Long sectionId,
        String departmentName
    );

    Optional<List<PreferTasToCourse>> findByReceiver_Name(String depName);

    Optional<List<PreferTasToCourse>> findBySender_Id(Long instrId);
}
