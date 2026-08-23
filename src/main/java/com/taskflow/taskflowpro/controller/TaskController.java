package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.entity.Comment;
import com.taskflow.taskflowpro.entity.Project;
import com.taskflow.taskflowpro.entity.Task;
import com.taskflow.taskflowpro.service.CommentService;
import com.taskflow.taskflowpro.service.ProjectService;
import com.taskflow.taskflowpro.service.TaskService;
import com.taskflow.taskflowpro.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;
    private final CommentService commentService;

    public TaskController(TaskService taskService,
                          ProjectService projectService,
                          UserService userService,
                          CommentService commentService) {

        this.taskService = taskService;
        this.projectService = projectService;
        this.userService = userService;
        this.commentService = commentService;
    }
    @GetMapping
    public String selectProject(Model model) {

        model.addAttribute("projects",
                projectService.getAllProjects());

        return "task-projects";
    }
    @GetMapping("/{projectId}")
    public String viewTasks(@PathVariable Long projectId,
                            Model model) {

        model.addAttribute("tasks",
                taskService.getTasksByProject(projectId));

        model.addAttribute("projectId", projectId);

        return "tasks";
    }

    @GetMapping("/new/{projectId}")
    public String createTaskForm(@PathVariable Long projectId,
                                 Model model) {

        Task task = new Task();

        Project project = projectService.getProjectById(projectId);

        task.setProject(project);

        model.addAttribute("task", task);

        model.addAttribute("users",
                userService.getAllUsers());

        return "create-task";
    }

    @PostMapping("/save")
    public String saveTask(@ModelAttribute Task task) {

        taskService.saveTask(task);

        return "redirect:/tasks/" + task.getProject().getId();
    }

    @GetMapping("/edit/{id}")
    public String editTask(@PathVariable Long id,
                           Model model) {

        Task task = taskService.getTaskById(id);

        model.addAttribute("task", task);

        model.addAttribute("users",
                userService.getAllUsers());

        return "edit-task";
    }

    @GetMapping("/delete/{id}/{projectId}")
    public String deleteTask(@PathVariable Long id,
                             @PathVariable Long projectId) {

        taskService.deleteTask(id);

        return "redirect:/tasks/" + projectId;
    }

    @GetMapping("/view/{id}")
    public String viewTaskDetails(@PathVariable Long id,
                                  Model model) {

        Task task = taskService.getTaskById(id);

        model.addAttribute("task", task);

        model.addAttribute("comments",
                commentService.getCommentsByTask(id));

        model.addAttribute("comment",
                new Comment());

        return "task-details";
    }

}

