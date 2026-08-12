package com.rgcet.admission.controller;

import com.rgcet.admission.dto.AdminProfileDto;
import com.rgcet.admission.dto.UpdateAdminProfileRequest;
import com.rgcet.admission.service.AdminProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class AdminProfileController {

    private final AdminProfileService profileService;

    public AdminProfileController(AdminProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<AdminProfileDto> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PutMapping
    public ResponseEntity<AdminProfileDto> updateProfile(@RequestBody UpdateAdminProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }
}
