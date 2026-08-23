package com.taskflow.taskflowpro.impl;

import com.taskflow.taskflowpro.entity.Project;
import com.taskflow.taskflowpro.repository.ProjectRepository;
import com.taskflow.taskflowpro.repository.TaskRepository;
import com.taskflow.taskflowpro.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void saveProject(Project project) {
        projectRepository.save(project);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    public void calculateProjectProgress(Project project) {

        long totalTasks = taskRepository.countByProjectId(project.getId());

        long completedTasks =
                taskRepository.countByProjectIdAndStatus(
                        project.getId(),
                        "Completed");

        if (totalTasks == 0) {
            project.setProgress(0);
        } else {
            int progress = (int) ((completedTasks * 100) / totalTasks);
            project.setProgress(progress);
        }
    }
}