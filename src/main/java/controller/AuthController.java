package com.taskflow.taskflowpro.controller;
import com.taskflow.taskflowpro.dto.RegisterRequest;
import com.taskflow.taskflowpro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(RegisterRequest request) {

        userService.registerUser(request);

        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
