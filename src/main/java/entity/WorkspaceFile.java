package com.taskflow.taskflowpro.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "workspace_files")
public class WorkspaceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String language;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    public WorkspaceFile() {
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}