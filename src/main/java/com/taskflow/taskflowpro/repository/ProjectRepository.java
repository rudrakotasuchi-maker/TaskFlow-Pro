package com.taskflow.taskflowpro.repository;

import com.taskflow.taskflowpro.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(String owner);

}