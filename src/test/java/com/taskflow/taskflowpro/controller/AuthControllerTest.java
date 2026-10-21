package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.dto.RegisterRequest;
import com.taskflow.taskflowpro.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testRegisterUser_Success_RedirectsToLoginWithFlashMessage() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Valid Student");
        request.setEmail("student@taskflow.com");
        request.setPassword("password123");
        request.setRole("STUDENT");
        request.setRegisterNumber("REG100");

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        Model model = new ConcurrentModel();

        String view = authController.registerUser(request, redirectAttributes, model);

        assertEquals("redirect:/auth/login", view);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("successMessage"));
        verify(userService, times(1)).registerUser(request);
    }

    @Test
    void testRegisterUser_DuplicateEmailOrValidationFailure_ReturnsRegisterViewWithErrorMessage() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Duplicate User");
        request.setEmail("existing@taskflow.com");

        doThrow(new IllegalArgumentException("An account with this email already exists"))
                .when(userService).registerUser(request);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        Model model = new ConcurrentModel();

        String view = authController.registerUser(request, redirectAttributes, model);

        assertEquals("register", view);
        assertEquals("An account with this email already exists", model.getAttribute("errorMessage"));
        assertSame(request, model.getAttribute("registerRequest"));
    }

    @Test
    void testRegisterPage_AddsDefaultRegisterRequest() {
        Model model = new ConcurrentModel();
        String view = authController.registerPage(model);

        assertEquals("register", view);
        assertNotNull(model.getAttribute("registerRequest"));
    }
}
