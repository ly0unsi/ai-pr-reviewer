package com.example.ai.pr.reviewer.service;

import com.example.ai.pr.reviewer.model.PullRequestReview;
import com.example.ai.pr.reviewer.model.SonarIssue;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodeReviewEngine {

    private final ChatClient chatClient;

    public CodeReviewEngine(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public PullRequestReview analyzePullRequest(String gitDiff, List<SonarIssue> sonarFindings) {
        // Enforce structured JSON mapping to PullRequestReview record
        BeanOutputConverter<PullRequestReview> outputConverter =
                new BeanOutputConverter<>(PullRequestReview.class);

        String systemPrompt = """
            You are an expert Senior Java Cloud Architect and Lead Code Reviewer.
            Your job is to perform a strict code review on the provided Git diff.
            
            Focus on:
            1. Security vulnerabilities (OWASP, SQL Injection, hardcoded secrets, insecure deserialization).
            2. Code quality & Java best practices (Liskov substitution, SOLID, resource leaks, null checks).
            3. Cloud performance & microservice patterns (N+1 queries, async handling, thread safety).
            4. SonarQube findings provided as context (if relevant to the diff).
            
            Be concise, direct, and constructive.
            """;

        String userPrompt = """
            Here are the current SonarQube findings for reference:
            {sonarContext}
            
            Here is the Git Diff to review:
            ```diff
            {gitDiff}
            ```
            
            {format}
            """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userSpec -> userSpec
                        .text(userPrompt)
                        .param("sonarContext", sonarFindings.toString())
                        .param("gitDiff", gitDiff)
                        .param("format", outputConverter.getFormat())
                )
                .call()
                .entity(outputConverter);
    }
}