package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.service.ProjectService;
import com.taskflow.taskflowpro.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProjectService projectService;
    private final TaskService taskService;

    public HomeController(ProjectService projectService,
                          TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("totalProjects",
                projectService.getAllProjects().size());

        model.addAttribute("totalTasks",
                taskService.getAllTasks().size());

        model.addAttribute("completedTasks",
                taskService.getCompletedTasksCount());

        model.addAttribute("pendingTasks",
                taskService.getPendingTasksCount());

        return "index";
    }
}