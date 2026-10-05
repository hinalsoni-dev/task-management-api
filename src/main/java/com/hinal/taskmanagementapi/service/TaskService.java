package com.hinal.taskmanagementapi.service;

import com.hinal.taskmanagementapi.dto.CreateTaskRequest;
import com.hinal.taskmanagementapi.dto.ReplaceTaskRequest;
import com.hinal.taskmanagementapi.dto.TaskResponse;
import com.hinal.taskmanagementapi.entity.Task;
import com.hinal.taskmanagementapi.entity.User;
import com.hinal.taskmanagementapi.exception.TaskNotFoundException;
import com.hinal.taskmanagementapi.repository.TaskRepository;
import com.hinal.taskmanagementapi.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse createTask(String username, CreateTaskRequest request) {
        User owner = findUser(username);
        Task task = new Task(
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                request.dueDate(),
                owner);
        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> getTasks(String username) {
        return taskRepository.findAllByOwner_Username(username).stream()
                .map(TaskService::toResponse)
                .toList();
    }

    public TaskResponse getTask(String username, Long id) {
        return toResponse(findTask(username, id));
    }

    public TaskResponse replaceTask(String username, Long id, ReplaceTaskRequest request) {
        Task task = findTask(username, id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(String username, Long id) {
        Task task = findTask(username, id);
        taskRepository.delete(task);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private Task findTask(String username, Long id) {
        return taskRepository.findByIdAndOwner_Username(id, username)
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
