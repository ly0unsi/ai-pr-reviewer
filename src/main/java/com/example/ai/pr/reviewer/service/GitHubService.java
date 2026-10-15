package com.example.ai.pr.reviewer.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GitHubService {

    private final WebClient githubWebClient;

    public GitHubService(@Qualifier("githubWebClient") WebClient githubWebClient) {
        this.githubWebClient = githubWebClient;
    }

    /**
     * Fetches raw unified Git diff for a Pull Request.
     */
    public String getPullRequestDiff(String owner, String repo, int prNumber) {
        return githubWebClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{prNumber}", owner, repo, prNumber)
                .header(HttpHeaders.ACCEPT, "application/vnd.github.v3.diff")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Posts the AI review report as a comment on the Pull Request.
     */
    public void postPRComment(String owner, String repo, int prNumber, String commentBody) {
        githubWebClient.post()
                .uri("/repos/{owner}/{repo}/issues/{prNumber}/comments", owner, repo, prNumber)
                .bodyValue(new CommentPayload(commentBody))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private record CommentPayload(String body) {}
}