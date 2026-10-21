package com.taskflow.taskflowpro.runner;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaRunner implements CodeRunner {

    @Override
    public String getLanguage() {
        return "java";
    }

    @Override
    public String run(String sourceCode, String input) throws Exception {

        // ==========================================
        // CREATE TEMPORARY FOLDER
        // ==========================================

        File tempDirectory =
                Files.createTempDirectory("taskflow_java").toFile();


        // ==========================================
        // FIND PUBLIC CLASS NAME
        // ==========================================

        Pattern pattern =
                Pattern.compile(
                        "public\\s+class\\s+([A-Za-z_$][A-Za-z0-9_$]*)"
                );

        Matcher matcher = pattern.matcher(sourceCode);

        String className;

        if (matcher.find()) {

            className = matcher.group(1);

        } else {

            // If no public class is found,
            // try normal class declaration.

            Pattern normalClassPattern =
                    Pattern.compile(
                            "class\\s+([A-Za-z_$][A-Za-z0-9_$]*)"
                    );

            Matcher normalMatcher =
                    normalClassPattern.matcher(sourceCode);

            if (normalMatcher.find()) {

                className = normalMatcher.group(1);

            } else {

                return "❌ Error: Could not find a Java class.";
            }
        }


        try {
            // ==========================================
            // CREATE JAVA FILE
            // ==========================================

            File javaFile =
                    new File(tempDirectory, className + ".java");


            // ==========================================
            // WRITE SOURCE CODE
            // ==========================================

            try (FileWriter writer =
                         new FileWriter(javaFile)) {

                writer.write(sourceCode);
            }


            // ==========================================
            // COMPILE JAVA PROGRAM
            // ==========================================

            ProcessBuilder compiler =
                    new ProcessBuilder(
                            "javac",
                            javaFile.getAbsolutePath()
                    );

            compiler.directory(tempDirectory);

            Process compileProcess =
                    compiler.start();

            boolean compileFinished =
                    compileProcess.waitFor(15, java.util.concurrent.TimeUnit.SECONDS);

            if (!compileFinished) {
                compileProcess.destroyForcibly();
                return "❌ Compilation Error\n\nCompilation timed out after 15 seconds.";
            }

            int compileExitCode =
                    compileProcess.exitValue();


            // ==========================================
            // COMPILATION ERROR
            // ==========================================

            if (compileExitCode != 0) {

                String errors =
                        new String(
                                compileProcess
                                        .getErrorStream()
                                        .readAllBytes()
                        );

                return "❌ Compilation Error\n\n" + errors;
            }


            // ==========================================
            // RUN JAVA PROGRAM
            // ==========================================

            ProcessBuilder runner =
                    new ProcessBuilder(
                            "java",
                            "-cp",
                            tempDirectory.getAbsolutePath(),
                            className
                    );

            runner.directory(tempDirectory);

            Process runProcess =
                    runner.start();


            // ==========================================
            // SEND PROGRAM INPUT
            // ==========================================

            if (input != null &&
                    !input.isEmpty()) {

                try {
                    runProcess
                            .getOutputStream()
                            .write(
                                    (input +
                                            System.lineSeparator())
                                            .getBytes()
                            );
                    runProcess.getOutputStream().flush();
                } catch (Exception ignored) {
                }
            }

            try {
                runProcess.getOutputStream().close();
            } catch (Exception ignored) {
            }


            // ==========================================
            // WAIT FOR PROGRAM (WITH TIMEOUT)
            // ==========================================

            boolean runFinished =
                    runProcess.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);

            if (!runFinished) {
                runProcess.destroyForcibly();
                return "❌ Runtime Error\n\nProgram execution timed out after 10 seconds.";
            }

            int runExitCode =
                    runProcess.exitValue();


            // ==========================================
            // READ OUTPUT
            // ==========================================

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


            // ==========================================
            // RUNTIME ERROR
            // ==========================================

            if (runExitCode != 0) {

                return "❌ Runtime Error\n\n" + (errors.isEmpty() ? output : errors);
            }


            // ==========================================
            // SUCCESS
            // ==========================================

            return output;

        } finally {
            // Clean up only temporary files/directories created by this execution
            deleteRecursively(tempDirectory);
        }
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        try {
            file.delete();
        } catch (Exception ignored) {
        }
    }
}