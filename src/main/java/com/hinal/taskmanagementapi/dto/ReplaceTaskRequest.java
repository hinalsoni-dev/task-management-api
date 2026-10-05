package com.hinal.taskmanagementapi.dto;

import com.hinal.taskmanagementapi.entity.TaskPriority;
import com.hinal.taskmanagementapi.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ReplaceTaskRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 120, message = "Title must be at most 120 characters")
        String title,
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,
        @NotNull(message = "Status is required")
        TaskStatus status,
        @NotNull(message = "Priority is required")
        TaskPriority priority,
        LocalDate dueDate) {
}
