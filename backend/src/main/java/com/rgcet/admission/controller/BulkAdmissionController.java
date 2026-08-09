package com.rgcet.admission.controller;

import com.rgcet.admission.dto.BulkAdmissionDtos.BulkAdmissionApplyResponse;
import com.rgcet.admission.dto.BulkAdmissionDtos.BulkAdmissionPreviewResponse;
import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdateRequest;
import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdateSchemaDto;
import com.rgcet.admission.service.BulkAdmissionSchemaService;
import com.rgcet.admission.service.BulkAdmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bulk-admission")
@RequiredArgsConstructor
public class BulkAdmissionController {

    private final BulkAdmissionSchemaService schemaService;
    private final BulkAdmissionService bulkAdmissionService;

    @GetMapping("/schema")
    public BulkUpdateSchemaDto schema() {
        return schemaService.getSchema();
    }

    @PostMapping("/validate")
    public BulkAdmissionPreviewResponse validate(@RequestBody BulkUpdateRequest request) {
        return bulkAdmissionService.validate(request);
    }

    @PostMapping("/apply")
    public BulkAdmissionApplyResponse apply(@RequestBody BulkUpdateRequest request) {
        return bulkAdmissionService.apply(request);
    }
}
