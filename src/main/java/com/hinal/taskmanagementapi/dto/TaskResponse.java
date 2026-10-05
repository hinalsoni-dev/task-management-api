package com.hinal.taskmanagementapi.dto;

import com.hinal.taskmanagementapi.entity.TaskPriority;
import com.hinal.taskmanagementapi.entity.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt) {
}
