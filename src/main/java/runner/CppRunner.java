package com.taskflow.taskflowpro.runner;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

@Component
public class CppRunner implements CodeRunner {

    @Override
    public String getLanguage() {
        return "cpp";
    }

    @Override
    public String run(String sourceCode, String input) throws Exception {

        File tempDirectory =
                Files.createTempDirectory("taskflow_cpp").toFile();

        File cppFile =
                new File(tempDirectory, "main.cpp");

        File executable =
                new File(tempDirectory, "main.exe");

        try (FileWriter writer = new FileWriter(cppFile)) {
            writer.write(sourceCode);
        }

        // Compile C++
        ProcessBuilder compiler =
                new ProcessBuilder(
                        "g++",
                        cppFile.getAbsolutePath(),
                        "-o",
                        executable.getAbsolutePath()
                );

        compiler.directory(tempDirectory);

        Process compileProcess =
                compiler.start();

        int compileExitCode =
                compileProcess.waitFor();

        if (compileExitCode != 0) {

            String errors =
                    new String(
                            compileProcess
                                    .getErrorStream()
                                    .readAllBytes()
                    );

            return "❌ Compilation Error\n\n" + errors;
        }

        // Run C++
        ProcessBuilder runner =
                new ProcessBuilder(
                        executable.getAbsolutePath()
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