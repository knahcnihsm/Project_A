package com.rgcet.admission.service;

import com.rgcet.admission.entity.BulkUploadLog;
import com.rgcet.admission.entity.BulkUploadStatus;
import com.rgcet.admission.repository.BulkUploadLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Persists bulk upload audit rows. Always runs in a NEW transaction so that a
 * FAILED log survives the rollback of the main commit transaction.
 */
@Service
@RequiredArgsConstructor
public class BulkUploadLogService {

    private final BulkUploadLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BulkUploadLog record(String fileName, String uploadedBy,
                                int totalRows, int validRows,
                                int updatedStudents, int noChangeRows,
                                int skippedRows, int failedRows,
                                long durationMs, BulkUploadStatus status) {
        BulkUploadLog log = new BulkUploadLog();
        log.setFileName(fileName);
        log.setUploadedBy(uploadedBy == null || uploadedBy.isBlank() ? "Admin" : uploadedBy.trim());
        log.setUploadedAt(LocalDateTime.now());
        log.setTotalRows(totalRows);
        log.setValidRows(validRows);
        log.setUpdatedStudents(updatedStudents);
        log.setSkippedRows(skippedRows);
        log.setFailedRows(failedRows);
        log.setDurationMs(durationMs);
        log.setStatus(status);
        return repository.save(log);
    }
}
