package com.rgcet.admission.dto;

import jakarta.validation.constraints.NotBlank;

public record ArchiveRequest(
        @NotBlank(message = "Archive reason is required") String reason,
        String description
) {
}
