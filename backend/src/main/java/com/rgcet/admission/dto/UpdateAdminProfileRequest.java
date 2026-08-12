package com.rgcet.admission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAdminProfileRequest {
    private String adminName;
    private String username;
    private String currentPassword;
    private String newPassword;
}