
package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.entity.Comment;
import com.taskflow.taskflowpro.entity.Task;
import com.taskflow.taskflowpro.entity.User;
import com.taskflow.taskflowpro.security.CustomUserDetails;
import com.taskflow.taskflowpro.service.CommentService;
import com.taskflow.taskflowpro.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final TaskService taskService;

    public CommentController(CommentService commentService,
                             TaskService taskService) {
        this.commentService = commentService;
        this.taskService = taskService;
    }

    @PostMapping("/save/{taskId}")
    public String saveComment(@PathVariable Long taskId,
                              @ModelAttribute Comment comment,
                              Authentication authentication) {

        Task task = taskService.getTaskById(taskId);

        comment.setTask(task);

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        comment.setUser(user);

        commentService.saveComment(comment);

        return "redirect:/tasks/view/" + taskId;
    }

}

