package com.example.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Task;

@Repository
public interface TaskRepo extends JpaRepository<Task, Integer>{
}
