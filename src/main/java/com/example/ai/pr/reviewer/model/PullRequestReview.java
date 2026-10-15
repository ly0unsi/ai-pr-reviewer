package com.example.ai.pr.reviewer.model;

import java.util.List;

public record PullRequestReview(
        String summary,
        int overallScore, // Score out of 100 based on code quality & security
        boolean approved,
        List<CodeIssue> issues,
        List<String> keyStrengths
) {}
