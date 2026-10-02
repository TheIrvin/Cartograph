package com.cartograph.api;

import com.cartograph.application.IndexRepositoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public final class IndexController {
    private final IndexRepositoryService service;

    public IndexController(IndexRepositoryService service) { this.service = service; }

    @PostMapping("/index")
    public ResponseEntity<GraphSnapshotResponse> index(@Valid @RequestBody IndexRepositoryRequest request) {
        return ResponseEntity.ok(GraphSnapshotResponse.from(service.index(request.repositoryUrl())));
    }
}
