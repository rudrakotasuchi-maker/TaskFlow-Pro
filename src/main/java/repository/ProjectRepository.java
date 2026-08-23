package com.taskflow.taskflowpro.repository;

import com.taskflow.taskflowpro.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}