package com.taskflow.taskflowpro.impl;

import com.taskflow.taskflowpro.runner.CodeRunner;
import com.taskflow.taskflowpro.service.RunCodeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RunCodeServiceImpl implements RunCodeService {

    private final List<CodeRunner> runners;

    public RunCodeServiceImpl(List<CodeRunner> runners) {
        this.runners = runners;
    }

    @Override
    public String runCode(String language,
                          String sourceCode,
                          String input) throws Exception {

        for (CodeRunner runner : runners) {

            if (runner.getLanguage().equalsIgnoreCase(language)) {

                return runner.run(sourceCode, input);

            }

        }

        return "Language not supported yet.";

    }
}