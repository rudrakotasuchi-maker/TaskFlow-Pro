package com.taskflow.taskflowpro.runner;

public interface CodeRunner {

    String getLanguage();

    String run(String sourceCode, String input) throws Exception;

}