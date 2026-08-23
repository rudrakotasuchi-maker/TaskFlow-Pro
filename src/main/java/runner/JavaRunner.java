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

        int compileExitCode =
                compileProcess.waitFor();


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

            runProcess
                    .getOutputStream()
                    .write(
                            (input +
                                    System.lineSeparator())
                                    .getBytes()
                    );
        }

        runProcess
                .getOutputStream()
                .flush();

        runProcess
                .getOutputStream()
                .close();


        // ==========================================
        // WAIT FOR PROGRAM
        // ==========================================

        int runExitCode =
                runProcess.waitFor();


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

            return "❌ Runtime Error\n\n" + errors;
        }


        // ==========================================
        // SUCCESS
        // ==========================================

        return output;
    }
}