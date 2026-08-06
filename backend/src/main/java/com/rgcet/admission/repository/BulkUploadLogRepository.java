package com.rgcet.admission.repository;

import com.rgcet.admission.entity.BulkUploadLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BulkUploadLogRepository extends JpaRepository<BulkUploadLog, Long> {

    List<BulkUploadLog> findByFileName(String fileName);
}
