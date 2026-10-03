package com.cartograph.api;

import com.cartograph.application.IndexRepositoryService;
import jakarta.validation.Valid;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes versioned HTTP endpoints for repository indexing. */
@RestController
@RequestMapping("/api/v1")
public final class IndexController {
    private final IndexRepositoryService service;

    /**
     * Creates the API controller.
     *
     * @param service application use case that indexes a repository URL
     */
    public IndexController(IndexRepositoryService service) { this.service = service; }

    /**
     * Indexes a GitHub repository and returns the graph for its resolved commit.
     *
     * @param request validated repository URL request
     * @return the generated graph snapshot response
     */
    @PostMapping("/index")
    public ResponseEntity<GraphSnapshotResponse> index(@Valid @RequestBody IndexRepositoryRequest request) {
        return ResponseEntity.ok(GraphSnapshotResponse.from(service.index(request.repositoryUrl())));
    }

    @GetMapping("/repositories/{owner}/{repo}")
    public ResponseEntity<GraphSnapshotResponse> snapshot(@PathVariable String owner, @PathVariable String repo) {
        return service.snapshot(owner + "/" + repo).map(GraphSnapshotResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("No indexed snapshot for " + owner + "/" + repo + "."));
    }
}
