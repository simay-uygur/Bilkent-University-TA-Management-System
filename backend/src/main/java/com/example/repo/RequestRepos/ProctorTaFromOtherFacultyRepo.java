package com.example.repo.RequestRepos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.ProctorTaFromOtherFaculty;

@Repository
public interface ProctorTaFromOtherFacultyRepo extends JpaRepository<ProctorTaFromOtherFaculty, Integer>{
    //List<ProctorTaFromOtherFaculty> findAllByReceiverIdAndIsPendingTrue()
}
