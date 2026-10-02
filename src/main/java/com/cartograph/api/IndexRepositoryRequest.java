package com.cartograph.api;

import jakarta.validation.constraints.NotBlank;

public record IndexRepositoryRequest(@NotBlank(message = "repositoryUrl must not be blank") String repositoryUrl) { }
