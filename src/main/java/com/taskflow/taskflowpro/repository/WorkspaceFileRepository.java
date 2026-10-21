package com.taskflow.taskflowpro.repository;

import com.taskflow.taskflowpro.entity.WorkspaceFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceFileRepository extends JpaRepository<WorkspaceFile, Long> {

    List<WorkspaceFile> findByProjectId(Long projectId);

}