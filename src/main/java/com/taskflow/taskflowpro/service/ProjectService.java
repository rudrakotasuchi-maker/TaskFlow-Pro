package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.entity.Project;

import java.util.List;

public interface ProjectService {

    void saveProject(Project project);

    List<Project> getAllProjects();

    Project getProjectById(Long id);

    void deleteProject(Long id);
    void calculateProjectProgress(Project project);

}