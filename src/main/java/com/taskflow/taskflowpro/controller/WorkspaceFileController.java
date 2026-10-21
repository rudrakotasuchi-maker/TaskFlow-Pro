package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.entity.WorkspaceFile;
import com.taskflow.taskflowpro.service.WorkspaceFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/workspace")
public class WorkspaceFileController {

    private final WorkspaceFileService workspaceFileService;

    public WorkspaceFileController(WorkspaceFileService workspaceFileService) {
        this.workspaceFileService = workspaceFileService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveFile(@RequestBody WorkspaceFile file, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            WorkspaceFile saved = workspaceFileService.saveFile(file, username);
            return ResponseEntity.ok(saved);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping({"/project", "/project/{projectId}"})
    public ResponseEntity<?> getFiles(@PathVariable(required = false) Long projectId, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            List<WorkspaceFile> files = workspaceFileService.getFilesByProject(projectId, username);
            return ResponseEntity.ok(files);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createFile(@RequestBody WorkspaceFile file, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            WorkspaceFile created = workspaceFileService.saveFile(file, username);
            return ResponseEntity.ok(created);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id, Principal principal) {
        try {
            String username = principal != null ? principal.getName() : null;
            workspaceFileService.deleteFile(id, username);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
