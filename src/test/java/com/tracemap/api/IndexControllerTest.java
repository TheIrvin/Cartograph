package com.tracemap.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracemap.application.IndexRepositoryService;
import com.tracemap.graph.model.GraphMetrics;
import com.tracemap.graph.model.GraphSnapshot;
import com.tracemap.ingestion.InvalidRepositoryUrlException;
import com.tracemap.ingestion.github.GitHubFetchException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IndexController.class)
@ContextConfiguration(classes = {IndexController.class, ApiExceptionHandler.class})
class IndexControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean IndexRepositoryService service;

    @Test
    void indexesRepositoryWithoutCallingGitHub() throws Exception {
        when(service.index("https://github.com/acme/widgets")).thenReturn(
                new GraphSnapshot("acme/widgets", "sha", List.of(), List.of(), List.of(), new GraphMetrics(0, 0, 0, 0)));

        mvc.perform(post("/api/v1/index").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new IndexRepositoryRequest("https://github.com/acme/widgets"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.repository").value("acme/widgets"))
                .andExpect(jsonPath("$.commitSha").value("sha"))
                .andExpect(jsonPath("$.metrics.filesSeen").value(0))
                .andExpect(jsonPath("$.metrics.filesParsed").value(0))
                .andExpect(jsonPath("$.metrics.nodes").value(0))
                .andExpect(jsonPath("$.metrics.edges").value(0));
    }

    @Test
    void rejectsBlankUrl() throws Exception {
        mvc.perform(post("/api/v1/index").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryUrl\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        verifyNoInteractions(service);
    }

    @Test
    void mapsMalformedUrlToStructuredBadRequest() throws Exception {
        when(service.index("not-a-url")).thenThrow(new InvalidRepositoryUrlException("bad"));
        mvc.perform(post("/api/v1/index").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryUrl\":\"not-a-url\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
    }

    @Test
    void mapsUnexpectedIllegalArgumentExceptionToInternalError() throws Exception {
        when(service.index("https://github.com/acme/widgets"))
                .thenThrow(new IllegalArgumentException("unexpected internal failure"));

        mvc.perform(post("/api/v1/index").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryUrl\":\"https://github.com/acme/widgets\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void mapsGitHubRateLimitToTooManyRequests() throws Exception {
        when(service.index("https://github.com/acme/widgets"))
                .thenThrow(new GitHubFetchException(GitHubFetchException.Kind.RATE_LIMITED, 429, "rate limited"));

        mvc.perform(post("/api/v1/index").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repositoryUrl\":\"https://github.com/acme/widgets\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("UPSTREAM_GITHUB_ERROR"));
    }
}
