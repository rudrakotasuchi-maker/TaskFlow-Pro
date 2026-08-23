package com.taskflow.taskflowpro.service;

public interface RunCodeService {

    String runCode(String language,
                   String sourceCode,
                   String input) throws Exception;

}