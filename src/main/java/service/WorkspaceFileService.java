package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.entity.WorkspaceFile;

import java.util.List;

public interface WorkspaceFileService {

    WorkspaceFile saveFile(WorkspaceFile file);

    List<WorkspaceFile> getFilesByProject(Long projectId);

    WorkspaceFile getFile(Long id);
    void deleteFile(Long id);

}