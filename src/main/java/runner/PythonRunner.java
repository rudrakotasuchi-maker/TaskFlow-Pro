package com.taskflow.taskflowpro.runner;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

@Component
public class PythonRunner implements CodeRunner {

    @Override
    public String getLanguage() {
        return "python";
    }

    @Override
    public String run(String sourceCode, String input) throws Exception {

        File tempDirectory =
                Files.createTempDirectory("taskflow_python").toFile();

        File pythonFile =
                new File(tempDirectory, "main.py");

        try (FileWriter writer =
                     new FileWriter(pythonFile)) {

            writer.write(sourceCode);
        }

        // Run Python
        ProcessBuilder runner =
                new ProcessBuilder(
                        "python",
                        pythonFile.getAbsolutePath()
                );

        runner.directory(tempDirectory);

        Process runProcess =
                runner.start();

        // Send input
        if (input != null && !input.isEmpty()) {

            runProcess
                    .getOutputStream()
                    .write(
                            (input + System.lineSeparator())
                                    .getBytes()
                    );
        }

        runProcess
                .getOutputStream()
                .flush();

        runProcess
                .getOutputStream()
                .close();

        int runExitCode =
                runProcess.waitFor();

        String output =
                new String(
                        runProcess
                                .getInputStream()
                                .readAllBytes()
                );

        String errors =
                new String(
                        runProcess
                                .getErrorStream()
                                .readAllBytes()
                );

        if (runExitCode != 0) {

            return "❌ Runtime Error\n\n" + errors;
        }

        return output;
    }
}