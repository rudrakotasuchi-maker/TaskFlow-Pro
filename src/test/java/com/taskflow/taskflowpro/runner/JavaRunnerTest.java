package com.taskflow.taskflowpro.runner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class JavaRunnerTest {

    private JavaRunner runner;

    @BeforeEach
    void setUp() {
        runner = new JavaRunner();
    }

    @Test
    void testSuccessfulExecution() throws Exception {
        String code = """
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello from TaskFlow!");
                    }
                }
                """;

        String output = runner.run(code, "");
        assertNotNull(output);
        assertTrue(output.contains("Hello from TaskFlow!"), "Output should contain greeting: " + output);
    }

    @Test
    void testExecutionWithInput() throws Exception {
        String code = """
                import java.util.Scanner;
                public class Echo {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        if (sc.hasNextLine()) {
                            System.out.println("Echo: " + sc.nextLine());
                        }
                    }
                }
                """;

        String output = runner.run(code, "TaskFlow rocks");
        assertNotNull(output);
        assertTrue(output.contains("Echo: TaskFlow rocks"), "Output should echo input: " + output);
    }

    @Test
    void testCompilationError() throws Exception {
        String code = """
                public class SyntaxError {
                    public static void main(String[] args) {
                        int x = ; // syntax error
                    }
                }
                """;

        String output = runner.run(code, "");
        assertNotNull(output);
        assertTrue(output.startsWith("❌ Compilation Error"), "Output should indicate compilation error: " + output);
    }

    @Test
    void testRuntimeError() throws Exception {
        String code = """
                public class RuntimeError {
                    public static void main(String[] args) {
                        throw new ArithmeticException("Divide by zero simulation");
                    }
                }
                """;

        String output = runner.run(code, "");
        assertNotNull(output);
        assertTrue(output.startsWith("❌ Runtime Error"), "Output should indicate runtime error: " + output);
        assertTrue(output.contains("ArithmeticException"), "Output should contain exception name: " + output);
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testInfiniteLoopTimeout() throws Exception {
        String code = """
                public class LoopForever {
                    public static void main(String[] args) {
                        while (true) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }
                """;

        String output = runner.run(code, "");
        assertNotNull(output);
        assertTrue(output.contains("timed out"), "Output should indicate timeout: " + output);
    }

    @Test
    void testCleanupTempDirectory() throws Exception {
        String code = """
                public class CleanupCheck {
                    public static void main(String[] args) {
                        System.out.println("Cleanup test");
                    }
                }
                """;

        runner.run(code, "");
        // Verify that no orphaned taskflow_java folders created in the last 2 seconds remain
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File[] taskflowDirs = tmpDir.listFiles((dir, name) -> name.startsWith("taskflow_java"));
        if (taskflowDirs != null) {
            long now = System.currentTimeMillis();
            for (File d : taskflowDirs) {
                // If a directory was created in the last 3 seconds, it should have been deleted
                if (now - d.lastModified() < 3000) {
                    assertFalse(d.exists(), "Temporary directory should have been cleaned up: " + d.getAbsolutePath());
                }
            }
        }
    }
}
