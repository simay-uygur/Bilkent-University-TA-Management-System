package com.example.demo1.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo1.entity.Task;

@Repository
public interface TaskRepo extends JpaRepository<Task, Integer>{
}
