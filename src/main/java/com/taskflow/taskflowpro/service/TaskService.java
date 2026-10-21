package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.entity.Task;

import java.util.List;

public interface TaskService {

    List<Task> getTasksByProject(Long projectId);

    List<Task> getAllTasks();

    void saveTask(Task task);

    Task getTaskById(Long id);

    long getCompletedTasksCount();

    long getPendingTasksCount();

    void deleteTask(Long id);
}