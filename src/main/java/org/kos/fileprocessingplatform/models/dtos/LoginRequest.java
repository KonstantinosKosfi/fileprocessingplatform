package org.kos.fileprocessingplatform.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String loginString,
        @NotBlank String password
) {
}
