package com.rgcet.admission.controller;

import com.rgcet.admission.dto.bulk.BulkCommitResponse;
import com.rgcet.admission.dto.bulk.BulkValidationResponse;
import com.rgcet.admission.dto.bulk.BulkWorkbookRequest;
import com.rgcet.admission.service.BulkStudentUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bulk")
@RequiredArgsConstructor
public class BulkUpdateController {

    private final BulkStudentUpdateService bulkStudentUpdateService;

    @PostMapping("/validate")
    public BulkValidationResponse validate(@RequestBody BulkWorkbookRequest request) {
        return bulkStudentUpdateService.validateAndPreview(request);
    }

    @PostMapping("/commit")
    public BulkCommitResponse commit(@RequestBody BulkWorkbookRequest request) {
        return bulkStudentUpdateService.commit(request);
    }
}
