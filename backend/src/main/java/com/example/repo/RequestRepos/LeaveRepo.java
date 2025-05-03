package com.example.repo.RequestRepos;

import org.springframework.stereotype.Repository;
import com.example.entity.Requests.Leave;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface LeaveRepo extends JpaRepository<Leave, Long> {
}

