package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.entity.Project;
import com.taskflow.taskflowpro.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String viewProjects(Model model) {

        List<Project> projects = projectService.getAllProjects();

        for (Project project : projects) {
            projectService.calculateProjectProgress(project);
        }

        model.addAttribute("projects", projects);

        return "projects";
    }

    @GetMapping("/new")
    public String createProjectForm(Model model) {

        model.addAttribute("project", new Project());

        return "create-project";
    }

    @PostMapping("/save")
    public String saveProject(@ModelAttribute Project project) {

        projectService.saveProject(project);

        return "redirect:/projects";
    }

    @GetMapping("/edit/{id}")
    public String editProject(@PathVariable Long id, Model model) {

        Project project = projectService.getProjectById(id);

        model.addAttribute("project", project);

        return "edit-project";
    }

    @GetMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id) {

        projectService.deleteProject(id);

        return "redirect:/projects";
    }
}