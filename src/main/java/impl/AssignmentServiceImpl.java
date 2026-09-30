package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.entity.Assignment;
import com.taskflow.taskflowpro.repository.AssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public Assignment createAssignment(Assignment assignment) {

        if (assignment.getCreatedDate() == null) {
            assignment.setCreatedDate(LocalDate.now());
        }

        return assignmentRepository.save(assignment);
    }

    @Override
    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    public Assignment getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Assignment not found with id: " + id));
    }

    @Override
    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }
}