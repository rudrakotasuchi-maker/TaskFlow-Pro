package com.taskflow.taskflowpro.controller;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.taskflow.taskflowpro.dto.AIRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {


    private final Client geminiClient;

    public AIController() {
        this.geminiClient = new Client();
    }

    @PostMapping("/ask")
    public String askAI(@RequestBody AIRequest request) {

        String message = request.getMessage();
        String code = request.getCode();

        String prompt = """
            You are TaskFlow AI, a helpful programming assistant inside a code editor.

            You can:
            - Explain programming code clearly.
            - Find and explain programming errors.
            - Generate useful code.
            - Suggest improvements.
            - Help beginners understand programming concepts.

            User request:
            %s

            Current code:
            %s

            Give a clear, practical answer.
            If the user asks about an error, explain the cause
            and provide corrected code when appropriate.
            """.formatted(
                message,
                code == null ? "" : code
        );

        try {

            //GenerateContentResponse response =
                   // geminiClient.models.generateContent(
                           // gemini-3.5-flash-lite,
                           // prompt,
                           // null
                    //);
            GenerateContentResponse response =
                    geminiClient.models.generateContent(
                            "gemini-3.5-flash-lite",
                            prompt,
                            null
                    );
            return response.text();

        } catch (Exception e) {

            e.printStackTrace();

            return "AI Error: " + e.getMessage();
        }
    }


}
