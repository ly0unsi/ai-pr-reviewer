package com.example.ai.pr.reviewer.service;

import com.example.ai.pr.reviewer.model.SonarIssue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
public class SonarQubeService {

    private final WebClient sonarWebClient;

    public SonarQubeService(
            @Value("${sonarqube.api.base-url}") String baseUrl,
            @Value("${sonarqube.api.token}") String token) {
        this.sonarWebClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    /**
     * Retrieves open security/quality findings for a specific project key.
     */
    public List<SonarIssue> getProjectIssues(String projectKey) {
        try {
            SonarSearchResponse response = sonarWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/issues/search")
                            .queryParam("componentKeys", projectKey)
                            .queryParam("statuses", "OPEN")
                            .build())
                    .retrieve()
                    .bodyToMono(SonarSearchResponse.class)
                    .block();

            return response != null ? response.issues() : Collections.emptyList();
        } catch (Exception e) {
            // Gracefully handle if SonarQube server is unavailable
            return Collections.emptyList();
        }
    }

    private record SonarSearchResponse(List<SonarIssue> issues) {}
}

