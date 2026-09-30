package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.entity.Assignment;
import com.taskflow.taskflowpro.entity.AssignmentSubmission;
import com.taskflow.taskflowpro.service.AssignmentService;
import com.taskflow.taskflowpro.service.AssignmentSubmissionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.taskflow.taskflowpro.entity.User;
import com.taskflow.taskflowpro.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService submissionService;
    private final UserRepository userRepository;
    public AssignmentController(
            AssignmentService assignmentService,
            AssignmentSubmissionService submissionService,
            UserRepository userRepository) {

        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.userRepository = userRepository;
    }

    // Show all assignments
    @GetMapping
    public String assignments(
            Model model,
            Authentication authentication) {

        model.addAttribute(
                "assignments",
                assignmentService.getAllAssignments()
        );

        String currentRole = authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");

        model.addAttribute("currentRole", currentRole);

        return "assignments";
    }

    // Show create assignment page
    @GetMapping("/create")
    public String createAssignmentForm(Model model) {

        model.addAttribute(
                "assignment",
                new Assignment()
        );

        return "assignment-create";
    }

    // Save assignment
    @PostMapping("/save")
    public String saveAssignment(
            @ModelAttribute Assignment assignment) {

        assignmentService.createAssignment(assignment);

        return "redirect:/assignments";
    }

    // View assignment
    @GetMapping("/{id}")
    public String viewAssignment(
            @PathVariable Long id,
            Model model,
            Authentication authentication) {

        model.addAttribute(
                "assignment",
                assignmentService.getAssignmentById(id)
        );

        String currentRole = getCurrentRole(authentication);

        System.out.println("=================================");
        System.out.println("LOGIN EMAIL: " + authentication.getName());
        System.out.println("AUTHORITIES: " + authentication.getAuthorities());
        System.out.println("CURRENT ROLE: " + currentRole);
        System.out.println("=================================");

        model.addAttribute("currentRole", currentRole);

        return "assignment-view";
    }
    private String getCurrentRole(Authentication authentication) {

        String role = authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority.getAuthority()
                )
                .orElse("");

        return role;
    }

    // Delete assignment
    @GetMapping("/delete/{id}")
    public String deleteAssignment(
            @PathVariable Long id) {

        assignmentService.deleteAssignment(id);

        return "redirect:/assignments";
    }

    // Show submission page
    @GetMapping("/{id}/submit")
    public String submitAssignmentForm(
            @PathVariable Long id,
            Model model) {

        Assignment assignment =
                assignmentService.getAssignmentById(id);

        AssignmentSubmission submission =
                new AssignmentSubmission();

        submission.setAssignmentId(id);

        model.addAttribute("assignment", assignment);
        model.addAttribute("submission", submission);

        return "assignment-submit";
    }

    // Save student submission
    @PostMapping("/{id}/submit")
    public String submitAssignment(
            @PathVariable Long id,
            @ModelAttribute AssignmentSubmission submission,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        // Always create a new submission
        submission.setId(null);

        // Link submission to assignment
        submission.setAssignmentId(id);

        // Get logged-in user
        String email = authentication.getName();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("Logged-in user not found");
        }

        submission.setStudentName(user.getFullName());
        submission.setStudentEmail(user.getEmail());

        // Check that student provided either answer or file
        if ((submission.getAnswer() == null
                || submission.getAnswer().trim().isEmpty())
                && (file == null || file.isEmpty())) {

            throw new RuntimeException(
                    "Please write an answer or upload a file."
            );
        }

        // Handle uploaded file
        if (file != null && !file.isEmpty()) {

            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null) {
                throw new RuntimeException("Invalid file name.");
            }

            String lowerFileName =
                    originalFileName.toLowerCase();

            // Allowed file formats
            if (!(lowerFileName.endsWith(".pdf")
                    || lowerFileName.endsWith(".doc")
                    || lowerFileName.endsWith(".docx")
                    || lowerFileName.endsWith(".ppt")
                    || lowerFileName.endsWith(".pptx")
                    || lowerFileName.endsWith(".zip"))) {

                throw new RuntimeException(
                        "Only PDF, DOC, DOCX, PPT, PPTX and ZIP files are allowed."
                );
            }

            try {

                // Create upload directory
                Path uploadDirectory =
                        Paths.get(
                                "uploads",
                                "assignments",
                                "submissions"
                        );

                Files.createDirectories(uploadDirectory);

                // Generate unique stored filename
                String storedFileName =
                        UUID.randomUUID()
                                + "_"
                                + originalFileName;

                Path filePath =
                        uploadDirectory.resolve(storedFileName);

                // Save file
                Files.copy(
                        file.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                // Store file information in database
                submission.setFileName(originalFileName);
                submission.setFilePath(
                        filePath.toString()
                );
                submission.setFileType(
                        file.getContentType()
                );

            } catch (IOException e) {

                throw new RuntimeException(
                        "Failed to save assignment file.",
                        e
                );
            }
        }

        // Save submission
        submissionService.submitAssignment(submission);

        return "redirect:/assignments/" + id;
    }


    // Faculty/Admin - View all submissions for an assignment
    @GetMapping("/{id}/submissions")
    public String viewSubmissions(
            @PathVariable Long id,
            Model model) {

        Assignment assignment =
                assignmentService.getAssignmentById(id);

        model.addAttribute(
                "assignment",
                assignment
        );

        model.addAttribute(
                "submissions",
                submissionService.getSubmissionsForAssignment(id)
        );

        return "assignment-submissions";

    }
    // View individual student submission
    @GetMapping("/submissions/{id}")
    public String viewSubmission(
            @PathVariable Long id,
            Model model) {

        AssignmentSubmission submission =
                submissionService.getSubmissionById(id);

        Assignment assignment =
                assignmentService.getAssignmentById(
                        submission.getAssignmentId()
                );

        model.addAttribute("submission", submission);
        model.addAttribute("assignment", assignment);

        return "assignment-submission-view";
    }
    // Show evaluation form
    @GetMapping("/submissions/{id}/evaluate")
    public String evaluateSubmissionForm(
            @PathVariable Long id,
            Model model) {

        AssignmentSubmission submission =
                submissionService.getSubmissionById(id);

        Assignment assignment =
                assignmentService.getAssignmentById(
                        submission.getAssignmentId()
                );

        model.addAttribute("submission", submission);
        model.addAttribute("assignment", assignment);

        return "assignment-evaluate";
    }


    // Save evaluation
    @PostMapping("/submissions/{id}/evaluate")
    public String evaluateSubmission(
            @PathVariable Long id,
            @RequestParam Integer marks,
            @RequestParam String feedback) {

        submissionService.evaluateSubmission(
                id,
                marks,
                feedback
        );

        AssignmentSubmission submission =
                submissionService.getSubmissionById(id);

        return "redirect:/assignments/submissions/"
                + submission.getId();
    }
    // Student - View own submission/result
    @GetMapping("/{id}/my-submission")
    public String viewMySubmission(
            @PathVariable Long id,
            Model model,
            Authentication authentication) {

        Assignment assignment =
                assignmentService.getAssignmentById(id);

        String email = authentication.getName();

        AssignmentSubmission submission =
                submissionService
                        .getSubmissionsByStudent(email)
                        .stream()
                        .filter(s -> s.getAssignmentId().equals(id))
                        .findFirst()
                        .orElse(null);

        model.addAttribute("assignment", assignment);
        model.addAttribute("submission", submission);

        return "assignment-my-submission";
    }
    @GetMapping("/submissions/{id}/download")
    public ResponseEntity<Resource> downloadSubmission(
            @PathVariable Long id) {

        AssignmentSubmission submission =
                submissionService.getSubmissionById(id);

        if (submission.getFilePath() == null
                || submission.getFilePath().isBlank()) {

            return ResponseEntity.notFound().build();
        }

        try {

            Path path =
                    Paths.get(submission.getFilePath())
                            .toAbsolutePath()
                            .normalize();

            Resource resource =
                    new UrlResource(path.toUri());

            if (!resource.exists()
                    || !resource.isReadable()) {

                return ResponseEntity.notFound().build();
            }

            String fileName = submission.getFileName();

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\""
                    )
                    .header(
                            HttpHeaders.CONTENT_TYPE,
                            submission.getFileType() != null
                                    ? submission.getFileType()
                                    : "application/octet-stream"
                    )
                    .body(resource);

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .build();
        }
    }
}