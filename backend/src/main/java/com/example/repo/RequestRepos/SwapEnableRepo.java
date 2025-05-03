package com.example.repo.RequestRepos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Requests.SwapEnable;

@Repository
public interface SwapEnableRepo extends JpaRepository<SwapEnable, Long> {
    // Custom query methods can be defined here if needed
    
}
