package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.entity.Assignment;

import java.util.List;

public interface AssignmentService {

    Assignment createAssignment(Assignment assignment);

    List<Assignment> getAllAssignments();

    Assignment getAssignmentById(Long id);

    void deleteAssignment(Long id);
}