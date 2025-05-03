package com.example.repo.RequestRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.TransferProctoring;

@Repository
public interface TransferProctoringRepo extends JpaRepository<TransferProctoring, Long> {
    // Custom query methods can be defined here if needed
    
}
