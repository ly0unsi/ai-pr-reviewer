package com.example.ai.pr.reviewer.model;



public record CodeIssue(
        String filePath,
        int lineNumber,
        Severity severity,        // CRITICAL, HIGH, MEDIUM, LOW, INFO
        Category category,        // SECURITY, PERFORMANCE, CODE_STYLE, BUG_RISK, ARCHITECTURE
        String issueSummary,
        String explanation,
        String suggestedFixCode
) {
    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW, INFO
    }

    public enum Category {
        SECURITY, PERFORMANCE, CODE_STYLE, BUG_RISK, ARCHITECTURE
    }
}
