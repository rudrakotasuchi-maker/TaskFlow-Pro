package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.entity.AssignmentSubmission;
import com.taskflow.taskflowpro.repository.AssignmentSubmissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentSubmissionServiceImpl
        implements AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;

    public AssignmentSubmissionServiceImpl(
            AssignmentSubmissionRepository submissionRepository) {

        this.submissionRepository = submissionRepository;
    }

    @Override
    public AssignmentSubmission submitAssignment(
            AssignmentSubmission submission) {

        AssignmentSubmission newSubmission =
                new AssignmentSubmission();

        newSubmission.setAssignmentId(
                submission.getAssignmentId()
        );

        newSubmission.setStudentName(
                submission.getStudentName()
        );

        newSubmission.setStudentEmail(
                submission.getStudentEmail()
        );

        newSubmission.setAnswer(submission.getAnswer());

        newSubmission.setFileName(submission.getFileName());
        newSubmission.setFilePath(submission.getFilePath());
        newSubmission.setFileType(submission.getFileType());

        newSubmission.setSubmittedAt(LocalDateTime.now());

        newSubmission.setStatus("SUBMITTED");

        newSubmission.setMarks(null);

        newSubmission.setFeedback(null);

        return submissionRepository.save(newSubmission);
    }

    @Override
    public List<AssignmentSubmission> getSubmissionsForAssignment(
            Long assignmentId) {

        return submissionRepository.findByAssignmentId(assignmentId);
    }

    @Override
    public List<AssignmentSubmission> getSubmissionsByStudent(
            String studentEmail) {

        return submissionRepository.findByStudentEmail(studentEmail);
    }

    @Override
    public AssignmentSubmission getSubmissionById(Long id) {

        return submissionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Submission not found with id: " + id
                        ));
    }
    @Override
    public AssignmentSubmission evaluateSubmission(
            Long submissionId,
            Integer marks,
            String feedback) {

        AssignmentSubmission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Submission not found with id: "
                                                + submissionId
                                ));

        submission.setMarks(marks);
        submission.setFeedback(feedback);
        submission.setStatus("GRADED");

        return submissionRepository.save(submission);
    }
}