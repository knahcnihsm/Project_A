package com.rgcet.admission.service;

import com.rgcet.admission.common.ResourceNotFoundException;
import com.rgcet.admission.entity.Certificate;
import com.rgcet.admission.entity.Student;
import com.rgcet.admission.entity.StudentCertificate;
import com.rgcet.admission.repository.CertificateRepository;
import com.rgcet.admission.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public String upload(Long studentId, Long certificateId, MultipartFile file) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));

        String filePath = fileStorageService.store(file);

        StudentCertificate studentCertificate = student.getCertificates().stream()
                .filter(c -> c.getCertificate() != null
                        && c.getCertificate().getCertificateId().equals(certificateId))
                .findFirst()
                .orElseGet(() -> {
                    StudentCertificate created = new StudentCertificate();
                    created.setStudent(student);
                    created.setCertificate(certificate);
                    student.getCertificates().add(created);
                    return created;
                });

        studentCertificate.setFilePath(filePath);
        studentCertificate.setIsSubmitted(true);
        studentCertificate.setUploadedAt(LocalDateTime.now());
        studentRepository.save(student);
        return filePath;
    }
}
