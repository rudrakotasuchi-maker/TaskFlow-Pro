package com.taskflow.taskflowpro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.taskflowpro.entity.Project;
import com.taskflow.taskflowpro.entity.User;
import com.taskflow.taskflowpro.entity.WorkspaceFile;
import com.taskflow.taskflowpro.impl.WorkspaceFileServiceImpl;
import com.taskflow.taskflowpro.repository.ProjectRepository;
import com.taskflow.taskflowpro.repository.UserRepository;
import com.taskflow.taskflowpro.repository.WorkspaceFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceFileServiceTest {

    @Mock
    private WorkspaceFileRepository fileRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private WorkspaceFileServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkspaceFileServiceImpl(fileRepository, projectRepository, userRepository);
    }

    @Test
    void testSaveNewFile_ResolvesProjectAndSaves() {
        Project project = new Project();
        project.setId(10L);
        project.setName("Existing Project");
        project.setOwner("user@taskflow.com");

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(fileRepository.save(any(WorkspaceFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkspaceFile newFile = new WorkspaceFile();
        newFile.setFileName("NewClass.java");
        newFile.setContent("public class NewClass {}");
        newFile.setLanguage("java");
        Project projectRef = new Project();
        projectRef.setId(10L);
        newFile.setProject(projectRef);

        WorkspaceFile saved = service.saveFile(newFile, "user@taskflow.com");

        assertNotNull(saved);
        assertEquals("NewClass.java", saved.getFileName());
        assertEquals(project, saved.getProject());
        verify(fileRepository, times(1)).save(newFile);
    }

    @Test
    void testUpdateExistingFile_UpdatesInPlaceWithoutDuplicates() {
        Project project = new Project();
        project.setId(1L);
        project.setOwner("owner@taskflow.com");

        WorkspaceFile existing = new WorkspaceFile();
        existing.setId(42L);
        existing.setFileName("OldName.java");
        existing.setContent("old content");
        existing.setLanguage("java");
        existing.setProject(project);

        when(fileRepository.findById(42L)).thenReturn(Optional.of(existing));
        when(fileRepository.save(any(WorkspaceFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkspaceFile updateRequest = new WorkspaceFile();
        updateRequest.setId(42L);
        updateRequest.setFileName("UpdatedName.java");
        updateRequest.setContent("new content");
        updateRequest.setLanguage("java");

        WorkspaceFile result = service.saveFile(updateRequest, "owner@taskflow.com");

        assertNotNull(result);
        assertEquals(42L, result.getId());
        assertEquals("UpdatedName.java", result.getFileName());
        assertEquals("new content", result.getContent());
        // Verify we saved the existing entity rather than inserting a new one
        verify(fileRepository, times(1)).save(existing);
    }

    @Test
    void testUpdateExistingFile_UnauthorizedUser_ThrowsAccessDeniedException() {
        Project project = new Project();
        project.setId(1L);
        project.setOwner("alice@taskflow.com");

        WorkspaceFile existing = new WorkspaceFile();
        existing.setId(42L);
        existing.setProject(project);

        when(fileRepository.findById(42L)).thenReturn(Optional.of(existing));
        User bob = new User();
        bob.setEmail("bob@taskflow.com");
        bob.setRole("STUDENT");
        when(userRepository.findByEmail("bob@taskflow.com")).thenReturn(bob);

        WorkspaceFile updateRequest = new WorkspaceFile();
        updateRequest.setId(42L);
        updateRequest.setContent("hacked content");

        assertThrows(AccessDeniedException.class, () -> service.saveFile(updateRequest, "bob@taskflow.com"));
        verify(fileRepository, never()).save(any());
    }

    @Test
    void testUpdateNonExistentFile_ThrowsIllegalArgumentException() {
        when(fileRepository.findById(999L)).thenReturn(Optional.empty());

        WorkspaceFile file = new WorkspaceFile();
        file.setId(999L);
        file.setContent("content");

        assertThrows(IllegalArgumentException.class, () -> service.saveFile(file, "user@taskflow.com"));
    }

    @Test
    void testResolveProject_NonExistentId_ThrowsIllegalArgumentException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.resolveProjectForUser(999L, "user@taskflow.com"));
    }

    @Test
    void testResolveProject_NoProjectExists_CreatesDefaultWorkspaceProject() {
        when(projectRepository.findByOwner("newUser@taskflow.com")).thenReturn(Collections.emptyList());
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        Project resolved = service.resolveProjectForUser(null, "newUser@taskflow.com");

        assertNotNull(resolved);
        assertEquals(100L, resolved.getId());
        assertEquals("newUser@taskflow.com", resolved.getOwner());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testJacksonDeserialization_WithSetId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
                {
                    "id": 25,
                    "fileName": "Test.java",
                    "content": "class Test {}",
                    "language": "java"
                }
                """;

        WorkspaceFile deserialized = mapper.readValue(json, WorkspaceFile.class);

        assertNotNull(deserialized);
        assertEquals(25L, deserialized.getId(), "Jackson should correctly set the ID on WorkspaceFile");
        assertEquals("Test.java", deserialized.getFileName());
    }
}
