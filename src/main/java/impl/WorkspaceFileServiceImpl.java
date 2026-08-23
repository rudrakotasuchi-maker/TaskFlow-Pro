package com.taskflow.taskflowpro.impl;

import com.taskflow.taskflowpro.entity.WorkspaceFile;
import com.taskflow.taskflowpro.repository.WorkspaceFileRepository;
import com.taskflow.taskflowpro.service.WorkspaceFileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkspaceFileServiceImpl implements WorkspaceFileService {

    private final WorkspaceFileRepository repository;

    public WorkspaceFileServiceImpl(WorkspaceFileRepository repository) {
        this.repository = repository;
    }

    @Override
    public WorkspaceFile saveFile(WorkspaceFile file) {
        return repository.save(file);
    }

    @Override
    public List<WorkspaceFile> getFilesByProject(Long projectId) {
        return repository.findByProjectId(projectId);
    }

    @Override
    public WorkspaceFile getFile(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void deleteFile(Long id) {

        repository.deleteById(id);

    }
}