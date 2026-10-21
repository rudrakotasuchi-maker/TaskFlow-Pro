package com.taskflow.taskflowpro.service;

import com.taskflow.taskflowpro.dto.RegisterRequest;
import com.taskflow.taskflowpro.entity.User;
import com.taskflow.taskflowpro.impl.UserServiceImpl;
import com.taskflow.taskflowpro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceRegistrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
        lenient().when(passwordEncoder.encode(any())).thenAnswer(invocation -> "encoded_" + invocation.getArgument(0));
    }

    @Test
    void testRegisterStudent_ValidFields_MappedCorrectly() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("securePassword123");
        request.setRole("STUDENT");
        request.setRegisterNumber("REG2026001");
        request.setBranch("Computer Science");
        request.setDepartment("CSE");
        request.setYear("IV B.Tech");
        request.setSection("A");

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(userRepository.existsByRegisterNumber("REG2026001")).thenReturn(false);

        userService.registerUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("John Doe", savedUser.getFullName());
        assertEquals("john.doe@example.com", savedUser.getEmail());
        assertEquals("encoded_securePassword123", savedUser.getPassword());
        assertEquals("STUDENT", savedUser.getRole());
        assertEquals("REG2026001", savedUser.getRegisterNumber());
        assertEquals("Computer Science", savedUser.getBranch());
        assertEquals("CSE", savedUser.getDepartment());
        assertEquals("IV B.Tech", savedUser.getYear());
        assertEquals("A", savedUser.getSection());
    }

    @Test
    void testRegisterFaculty_EmptyRegisterNumber_NormalizedToNull() {
        // This test specifically validates the fix for 'Duplicate entry '' for key users.UKcqhf01uh9dowjtif83jcbg22k'
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Dr. Jane Smith");
        request.setEmail("jane.smith@univ.edu");
        request.setPassword("facultyPass999");
        request.setRole("FACULTY");
        request.setRegisterNumber(""); // Empty string submitted from browser
        request.setDepartment("AI & DS");

        when(userRepository.existsByEmail("jane.smith@univ.edu")).thenReturn(false);

        userService.registerUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("Dr. Jane Smith", savedUser.getFullName());
        assertEquals("jane.smith@univ.edu", savedUser.getEmail());
        assertEquals("FACULTY", savedUser.getRole());
        assertNull(savedUser.getRegisterNumber(), "Empty register number must be normalized to null so MySQL unique constraint does not collide");
        assertEquals("AI & DS", savedUser.getDepartment());
    }

    @Test
    void testRegisterAdmin_WhitespaceRegisterNumber_NormalizedToNull() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Admin User");
        request.setEmail("admin@taskflow.com");
        request.setPassword("adminSecret");
        request.setRole("ADMIN");
        request.setRegisterNumber("    ");

        when(userRepository.existsByEmail("admin@taskflow.com")).thenReturn(false);

        userService.registerUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNull(captor.getValue().getRegisterNumber());
    }

    @Test
    void testRegisterStudent_MissingRegisterNumber_ThrowsIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Student Without Reg");
        request.setEmail("student@example.com");
        request.setPassword("pass123");
        request.setRole("STUDENT");
        request.setRegisterNumber(""); // Empty string for student

        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("Register number is required for students", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterStudent_NullRegisterNumber_ThrowsIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Student Null Reg");
        request.setEmail("student2@example.com");
        request.setPassword("pass123");
        request.setRole("STUDENT");
        request.setRegisterNumber(null);

        when(userRepository.existsByEmail("student2@example.com")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("Register number is required for students", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_MissingFullName_ThrowsIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("   ");
        request.setEmail("test@example.com");
        request.setPassword("pass123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("Full name is required", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_MissingEmail_ThrowsIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Valid Name");
        request.setEmail("   ");
        request.setPassword("pass123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("Email is required", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_MissingPassword_ThrowsIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Valid Name");
        request.setEmail("test@example.com");
        request.setPassword("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("Password is required", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsFriendlyException() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Another Person");
        request.setEmail("existing@example.com");
        request.setPassword("pass123");
        request.setRole("STUDENT");
        request.setRegisterNumber("REG999");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("An account with this email already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_DuplicateRegisterNumber_ThrowsFriendlyException() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Another Student");
        request.setEmail("newstudent@example.com");
        request.setPassword("pass123");
        request.setRole("STUDENT");
        request.setRegisterNumber("REG_DUPLICATE");

        when(userRepository.existsByEmail("newstudent@example.com")).thenReturn(false);
        when(userRepository.existsByRegisterNumber("REG_DUPLICATE")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("An account with this register number already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_DataIntegrityViolation_DuplicateHandledGracefully() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Race Student");
        request.setEmail("race@example.com");
        request.setPassword("pass123");
        request.setRole("STUDENT");
        request.setRegisterNumber("REG_RACE");

        when(userRepository.existsByEmail("race@example.com")).thenReturn(false, true); // second call in catch block
        when(userRepository.existsByRegisterNumber("REG_RACE")).thenReturn(false);
        doThrow(new DataIntegrityViolationException("Duplicate key error")).when(userRepository).save(any());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request);
        });

        assertEquals("An account with this email already exists", ex.getMessage());
    }
}
