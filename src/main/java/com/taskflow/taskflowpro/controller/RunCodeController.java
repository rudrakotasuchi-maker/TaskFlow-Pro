package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.service.RunCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/run")
public class RunCodeController {

    private final RunCodeService runCodeService;

    public RunCodeController(RunCodeService runCodeService) {
        this.runCodeService = runCodeService;
    }

    @PostMapping
    public ResponseEntity<String> runCode(

            @RequestParam String language,

            @RequestParam(required = false, defaultValue = "") String input,

            @RequestBody String sourceCode

    ) {

        try {

            String result =
                    runCodeService.runCode(
                            language,
                            sourceCode,
                            input
                    );

            // ==========================================
            // COMPILATION ERROR
            // ==========================================

            if (result.startsWith("❌ Compilation Error")) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(result);
            }

            // ==========================================
            // RUNTIME ERROR
            // ==========================================

            if (result.startsWith("❌ Runtime Error")) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(result);
            }

            // ==========================================
            // OTHER CODE ERROR
            // ==========================================

            if (result.startsWith("❌ Error")) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(result);
            }

            // ==========================================
            // SUCCESS
            // ==========================================

            return ResponseEntity
                    .ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "❌ Server Error\n\n" +
                                    e.getMessage()
                    );
        }
    }
}