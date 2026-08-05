package com.rgcet.admission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CommunicationStepRequest(
        @Valid @NotNull AddressRequest permanentAddress,
        @Valid @NotNull AddressRequest communicationAddress,
        boolean sameAsPermanent
) {
    public record AddressRequest(
            String addressLine,
            @Pattern(regexp = "\\d{6}", message = "PIN code must be 6 digits") String pincode,
            @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits") String phone,
            @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits") String mobile,
            String email
    ) {
    }
}
