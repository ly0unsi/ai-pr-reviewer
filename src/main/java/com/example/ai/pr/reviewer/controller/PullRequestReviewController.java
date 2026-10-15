package com.example.ai.pr.reviewer.controller;

import com.example.ai.pr.reviewer.model.CodeIssue;
import com.example.ai.pr.reviewer.model.PullRequestReview;
import com.example.ai.pr.reviewer.model.SonarIssue;
import com.example.ai.pr.reviewer.service.CodeReviewEngine;
import com.example.ai.pr.reviewer.service.GitHubService;
import com.example.ai.pr.reviewer.service.SonarQubeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pr")
public class PullRequestReviewController {

    private final GitHubService gitHubService;
    private final SonarQubeService sonarQubeService;
    private final CodeReviewEngine codeReviewEngine;

    public PullRequestReviewController(GitHubService gitHubService,
                                       SonarQubeService sonarQubeService,
                                       CodeReviewEngine codeReviewEngine) {
        this.gitHubService = gitHubService;
        this.sonarQubeService = sonarQubeService;
        this.codeReviewEngine = codeReviewEngine;
    }

    @PostMapping("/review")
    public ResponseEntity<PullRequestReview> triggerReview(
            @RequestParam String owner,
            @RequestParam String repo,
            @RequestParam int prNumber,
            @RequestParam(required = false) String sonarProjectKey) {

        // 1. Fetch Git Diff
        String gitDiff = gitHubService.getPullRequestDiff(owner, repo, prNumber);

        // 2. Fetch SonarQube Findings
        List<SonarIssue> sonarIssues = (sonarProjectKey != null)
                ? sonarQubeService.getProjectIssues(sonarProjectKey)
                : List.of();

        // 3. Analyze with Bedrock Claude 3.5 Sonnet
        PullRequestReview review = codeReviewEngine.analyzePullRequest(gitDiff, sonarIssues);

        // 4. Format review as Markdown and post back to GitHub
        String markdownComment = formatReviewToMarkdown(review);
        gitHubService.postPRComment(owner, repo, prNumber, markdownComment);

        return ResponseEntity.ok(review);
    }

    private String formatReviewToMarkdown(PullRequestReview review) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 🤖 AI Code Review Summary\n\n");
        sb.append("**Overall Score:** ").append(review.overallScore()).append("/100\n");
        sb.append("**Status:** ").append(review.approved() ? "✅ Approved" : "⚠️ Changes Requested").append("\n\n");
        sb.append("### Summary\n").append(review.summary()).append("\n\n");

        if (!review.keyStrengths().isEmpty()) {
            sb.append("### 💪 Key Strengths\n");
            review.keyStrengths().forEach(s -> sb.append("- ").append(s).append("\n"));
            sb.append("\n");
        }

        if (!review.issues().isEmpty()) {
            sb.append("### 🔍 Code Issues & Security Vulnerabilities\n");
            for (CodeIssue issue : review.issues()) {
                sb.append("#### [").append(issue.severity()).append("] ").append(issue.issueSummary()).append("\n");
                sb.append("- **File:** `").append(issue.filePath()).append("` (Line ").append(issue.lineNumber()).append(")\n");
                sb.append("- **Category:** ").append(issue.category()).append("\n");
                sb.append("- **Explanation:** ").append(issue.explanation()).append("\n");
                if (issue.suggestedFixCode() != null && !issue.suggestedFixCode().isBlank()) {
                    sb.append("```java\n").append(issue.suggestedFixCode()).append("\n```\n");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}