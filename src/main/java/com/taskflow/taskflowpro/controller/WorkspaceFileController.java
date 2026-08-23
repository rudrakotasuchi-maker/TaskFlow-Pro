package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.entity.WorkspaceFile;
import com.taskflow.taskflowpro.service.WorkspaceFileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workspace")
public class WorkspaceFileController {

    private final WorkspaceFileService workspaceFileService;

    public WorkspaceFileController(WorkspaceFileService workspaceFileService) {
        this.workspaceFileService = workspaceFileService;
    }

    @PostMapping("/save")
    public WorkspaceFile saveFile(@RequestBody WorkspaceFile file) {
        return workspaceFileService.saveFile(file);
    }

    @GetMapping("/project/{projectId}")
    public List<WorkspaceFile> getFiles(@PathVariable Long projectId) {
        return workspaceFileService.getFilesByProject(projectId);
    }
    @PostMapping("/create")
    public WorkspaceFile createFile(@RequestBody WorkspaceFile file) {

        return workspaceFileService.saveFile(file);

    }

    @DeleteMapping("/{id}")
    public void deleteFile(@PathVariable Long id) {

        System.out.println("DELETE REQUEST RECEIVED FOR ID = " + id);

        workspaceFileService.deleteFile(id);

    }
}