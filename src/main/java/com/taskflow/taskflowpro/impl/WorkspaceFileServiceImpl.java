package com.taskflow.taskflowpro.impl;

import com.taskflow.taskflowpro.entity.Project;
import com.taskflow.taskflowpro.entity.User;
import com.taskflow.taskflowpro.entity.WorkspaceFile;
import com.taskflow.taskflowpro.repository.ProjectRepository;
import com.taskflow.taskflowpro.repository.UserRepository;
import com.taskflow.taskflowpro.repository.WorkspaceFileRepository;
import com.taskflow.taskflowpro.service.WorkspaceFileService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkspaceFileServiceImpl implements WorkspaceFileService {

    private final WorkspaceFileRepository repository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public WorkspaceFileServiceImpl(WorkspaceFileRepository repository,
                                    ProjectRepository projectRepository,
                                    UserRepository userRepository) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean isUserAuthorizedForProject(Project project, String username) {
        if (project == null) {
            return false;
        }
        if (username == null || username.isBlank()) {
            return false;
        }
        if (project.getOwner() == null || project.getOwner().isBlank()) {
            return true;
        }
        if (project.getOwner().equalsIgnoreCase(username)) {
            return true;
        }
        User user = userRepository.findByEmail(username);
        if (user != null) {
            String role = user.getRole();
            if ("ADMIN".equalsIgnoreCase(role) || "FACULTY".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role)) {
                return true;
            }
            if (user.getFullName() != null && project.getOwner().equalsIgnoreCase(user.getFullName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Project resolveProjectForUser(Long projectId, String username) {
        if (projectId != null) {
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                throw new IllegalArgumentException("Project not found with id: " + projectId);
            }
            if (!isUserAuthorizedForProject(project, username)) {
                throw new AccessDeniedException("You do not have permission to access project: " + projectId);
            }
            return project;
        }

        // If no projectId provided, resolve the user's active/owned project
        if (username != null && !username.isBlank()) {
            List<Project> owned = projectRepository.findByOwner(username);
            if (!owned.isEmpty()) {
                return owned.get(0);
            }
            User user = userRepository.findByEmail(username);
            if (user != null && user.getFullName() != null) {
                List<Project> byName = projectRepository.findByOwner(user.getFullName());
                if (!byName.isEmpty()) {
                    return byName.get(0);
                }
            }
        }

        // Check if any legacy project without owner exists
        List<Project> all = projectRepository.findAll();
        for (Project p : all) {
            if (p.getOwner() == null || p.getOwner().isBlank()) {
                if (username != null) {
                    p.setOwner(username);
                    return projectRepository.save(p);
                }
                return p;
            }
        }

        // Create an authorized default project for this user
        Project defaultProject = new Project();
        defaultProject.setName("My Workspace");
        defaultProject.setDescription("Default workspace project");
        defaultProject.setOwner(username != null ? username : "TaskFlow User");
        defaultProject.setDeadline("Ongoing");
        return projectRepository.save(defaultProject);
    }

    @Override
    public WorkspaceFile saveFile(WorkspaceFile file) {
        return saveFile(file, null);
    }

    @Override
    public WorkspaceFile saveFile(WorkspaceFile file, String username) {
        if (file == null) {
            throw new IllegalArgumentException("WorkspaceFile cannot be null");
        }

        // Case 1: Updating an existing file
        if (file.getId() != null) {
            WorkspaceFile existing = repository.findById(file.getId()).orElse(null);
            if (existing == null) {
                throw new IllegalArgumentException("File not found with id: " + file.getId());
            }
            if (existing.getProject() != null && !isUserAuthorizedForProject(existing.getProject(), username)) {
                throw new AccessDeniedException("You do not have permission to modify this file.");
            }
            if (file.getFileName() != null && !file.getFileName().isBlank()) {
                existing.setFileName(file.getFileName());
            }
            if (file.getContent() != null) {
                existing.setContent(file.getContent());
            }
            if (file.getLanguage() != null && !file.getLanguage().isBlank()) {
                existing.setLanguage(file.getLanguage());
            }
            return repository.save(existing);
        }

        // Case 2: Creating a new file
        Long targetProjectId = (file.getProject() != null) ? file.getProject().getId() : null;
        Project project = resolveProjectForUser(targetProjectId, username);
        file.setProject(project);
        return repository.save(file);
    }

    @Override
    public List<WorkspaceFile> getFilesByProject(Long projectId) {
        return repository.findByProjectId(projectId);
    }

    @Override
    public List<WorkspaceFile> getFilesByProject(Long projectId, String username) {
        Project project = resolveProjectForUser(projectId, username);
        return repository.findByProjectId(project.getId());
    }

    @Override
    public WorkspaceFile getFile(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public WorkspaceFile getFile(Long id, String username) {
        WorkspaceFile file = repository.findById(id).orElse(null);
        if (file != null && file.getProject() != null && !isUserAuthorizedForProject(file.getProject(), username)) {
            throw new AccessDeniedException("You do not have permission to view this file.");
        }
        return file;
    }

    @Override
    public void deleteFile(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteFile(Long id, String username) {
        WorkspaceFile file = repository.findById(id).orElse(null);
        if (file == null) {
            return;
        }
        if (file.getProject() != null && !isUserAuthorizedForProject(file.getProject(), username)) {
            throw new AccessDeniedException("You do not have permission to delete this file.");
        }
        repository.deleteById(id);
    }
}
