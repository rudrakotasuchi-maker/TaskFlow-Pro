package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.entity.AssignmentSubmission;

import java.util.List;

public interface AssignmentSubmissionService {

    AssignmentSubmission submitAssignment(
            AssignmentSubmission submission
    );

    List<AssignmentSubmission> getSubmissionsForAssignment(
            Long assignmentId
    );

    List<AssignmentSubmission> getSubmissionsByStudent(
            String studentEmail
    );

    AssignmentSubmission getSubmissionById(Long id);
    AssignmentSubmission evaluateSubmission(
            Long submissionId,
            Integer marks,
            String feedback
    );
}