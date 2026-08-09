package com.rgcet.admission.controller;

import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdateApplyResponse;
import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdatePreviewResponse;
import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdateRequest;
import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdateSchemaDto;
import com.rgcet.admission.service.BulkUpdateSchemaService;
import com.rgcet.admission.service.BulkUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bulk-update")
@RequiredArgsConstructor
public class BulkUpdateController {

    private final BulkUpdateSchemaService schemaService;
    private final BulkUpdateService bulkUpdateService;

    @GetMapping("/schema")
    public BulkUpdateSchemaDto schema() {
        return schemaService.getSchema();
    }

    @PostMapping("/validate")
    public BulkUpdatePreviewResponse validate(@RequestBody BulkUpdateRequest request) {
        return bulkUpdateService.validate(request);
    }

    @PostMapping("/apply")
    public BulkUpdateApplyResponse apply(@RequestBody BulkUpdateRequest request) {
        return bulkUpdateService.apply(request);
    }
}
