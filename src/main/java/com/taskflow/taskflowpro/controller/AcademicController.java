package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AcademicController {

    @GetMapping("/academic/profile")
    public String academicProfile(Authentication authentication, Model model) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        model.addAttribute("user", userDetails.getUser());

        return "academic-profile";
    }
}