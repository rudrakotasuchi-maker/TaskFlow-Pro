package com.taskflow.taskflowpro.repository;

import com.taskflow.taskflowpro.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}