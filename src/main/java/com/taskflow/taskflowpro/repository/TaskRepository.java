package com.taskflow.taskflowpro.repository;

import com.taskflow.taskflowpro.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    long countByStatus(String status);
    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, String status);

}