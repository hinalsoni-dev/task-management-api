package com.hinal.taskmanagementapi.repository;

import com.hinal.taskmanagementapi.entity.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOwner_Username(String username);

    Optional<Task> findByIdAndOwner_Username(Long id, String username);
}
