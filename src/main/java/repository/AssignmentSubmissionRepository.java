package com.taskflow.taskflowpro.repository;

import com.taskflow.taskflowpro.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    List<AssignmentSubmission> findByStudentEmail(String studentEmail);

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentEmail(
            Long assignmentId,
            String studentEmail
    );
}