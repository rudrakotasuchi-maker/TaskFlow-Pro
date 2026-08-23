package com.taskflow.taskflowpro.impl;

import com.taskflow.taskflowpro.entity.Task;
import com.taskflow.taskflowpro.repository.TaskRepository;
import com.taskflow.taskflowpro.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    @Override
    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @Override
    public long getCompletedTasksCount() {
        return taskRepository.countByStatus("Completed");
    }

    @Override
    public long getPendingTasksCount() {
        return taskRepository.countByStatus("Pending");
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}