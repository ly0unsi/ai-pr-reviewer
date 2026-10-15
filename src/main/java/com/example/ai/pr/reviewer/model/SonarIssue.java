package com.example.ai.pr.reviewer.model;

public record SonarIssue(
        String component,
        int line,
        String message,
        String severity,
        String type
) {}
