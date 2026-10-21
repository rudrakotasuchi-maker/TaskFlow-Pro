
package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.dto.StudioFile;
import com.taskflow.taskflowpro.entity.Project;
import com.taskflow.taskflowpro.service.WorkspaceFileService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/studio")
public class CodeStudioController {

    private final WorkspaceFileService workspaceFileService;

    public CodeStudioController(WorkspaceFileService workspaceFileService) {
        this.workspaceFileService = workspaceFileService;
    }

    // Open active/default Studio for current user
    @GetMapping("/active")
    public String studioActive(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        Project project = workspaceFileService.resolveProjectForUser(null, username);
        return "redirect:/studio/" + project.getId();
    }

    // Open Studio for specific project with authorization check
    @GetMapping("/{projectId}")
    public String studio(@PathVariable Long projectId,
                         Model model,
                         Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            Project project = workspaceFileService.resolveProjectForUser(projectId, username);
            model.addAttribute("projectId", project.getId());
            model.addAttribute("projectName", project.getName());
            return "studio";
        } catch (AccessDeniedException e) {
            return "redirect:/projects?error=unauthorized";
        } catch (IllegalArgumentException e) {
            // Project does not exist, redirect to active project
            return "redirect:/studio/active";
        }
    }

    // Save file
    @PostMapping("/save")
    @ResponseBody
    public String saveFile(@RequestBody StudioFile studioFile)
            throws IOException {

        File folder = new File("workspace");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, studioFile.getFileName());

        FileWriter writer = new FileWriter(file);

        writer.write(studioFile.getContent());

        writer.close();

        return "Saved Successfully!";
    }
}

