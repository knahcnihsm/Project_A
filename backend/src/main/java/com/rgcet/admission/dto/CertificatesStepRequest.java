package com.rgcet.admission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CertificatesStepRequest(
        @Valid List<CertificateItem> certificates
) {
    public record CertificateItem(
            @NotNull Long certificateId,
            boolean submitted,
            String filePath
    ) {
    }
}
