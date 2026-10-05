package com.hinal.taskmanagementapi.service;

import com.hinal.taskmanagementapi.dto.CreateTaskRequest;
import com.hinal.taskmanagementapi.dto.ReplaceTaskRequest;
import com.hinal.taskmanagementapi.dto.TaskResponse;
import com.hinal.taskmanagementapi.entity.Task;
import com.hinal.taskmanagementapi.exception.TaskNotFoundException;
import com.hinal.taskmanagementapi.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = new Task(
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                request.dueDate());
        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> getTasks() {
        return taskRepository.findAll().stream()
                .map(TaskService::toResponse)
                .toList();
    }

    public TaskResponse getTask(Long id) {
        return toResponse(findTask(id));
    }

    public TaskResponse replaceTask(Long id, ReplaceTaskRequest request) {
        Task task = findTask(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        Task task = findTask(id);
        taskRepository.delete(task);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
