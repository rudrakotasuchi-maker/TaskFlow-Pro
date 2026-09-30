package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.entity.User;
import com.taskflow.taskflowpro.repository.UserRepository;
import com.taskflow.taskflowpro.service.AssignmentService;
import com.taskflow.taskflowpro.service.ProjectService;
import com.taskflow.taskflowpro.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final AssignmentService assignmentService;
    private final UserRepository userRepository;

    public HomeController(
            ProjectService projectService,
            TaskService taskService,
            AssignmentService assignmentService,
            UserRepository userRepository) {

        this.projectService = projectService;
        this.taskService = taskService;
        this.assignmentService = assignmentService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(
            Model model,
            Authentication authentication) {

        // Projects
        model.addAttribute(
                "totalProjects",
                projectService.getAllProjects().size()
        );

        // Tasks
        model.addAttribute(
                "totalTasks",
                taskService.getAllTasks().size()
        );

        model.addAttribute(
                "completedTasks",
                taskService.getCompletedTasksCount()
        );

        model.addAttribute(
                "pendingTasks",
                taskService.getPendingTasksCount()
        );

        // Assignments
        model.addAttribute(
                "totalAssignments",
                assignmentService.getAllAssignments().size()
        );

        // Current logged-in user
        String email = authentication.getName();

        User currentUser =
                userRepository.findByEmail(email);

        model.addAttribute(
                "currentUser",
                currentUser
        );

        return "index";
    }
    @GetMapping("/learning")
    public String learning() {
        return "learning";
    }
    @GetMapping("/learning/platforms")
    public String learningPlatforms() {
        return "learning-platforms";
    }
    @GetMapping("/learning/academy")
    public String codingAcademy() {
        return "coding-academy";
    }
    @GetMapping("/learning/academy/dsa")
    public String dsaRoadmap() {
        return "dsa-roadmap";
    }
    @GetMapping("/studio")
    public String studio() {
        return "studio";
    }
}