package com.hinal.taskmanagementapi.repository;

import com.hinal.taskmanagementapi.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
