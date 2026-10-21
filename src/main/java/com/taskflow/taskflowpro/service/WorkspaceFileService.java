package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.entity.Project;
import com.taskflow.taskflowpro.entity.WorkspaceFile;

import java.util.List;

public interface WorkspaceFileService {

    WorkspaceFile saveFile(WorkspaceFile file);

    WorkspaceFile saveFile(WorkspaceFile file, String username);

    List<WorkspaceFile> getFilesByProject(Long projectId);

    List<WorkspaceFile> getFilesByProject(Long projectId, String username);

    WorkspaceFile getFile(Long id);

    WorkspaceFile getFile(Long id, String username);

    void deleteFile(Long id);

    void deleteFile(Long id, String username);

    Project resolveProjectForUser(Long projectId, String username);

    boolean isUserAuthorizedForProject(Project project, String username);
}
