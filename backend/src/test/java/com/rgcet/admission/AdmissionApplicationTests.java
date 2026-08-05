package com.rgcet.admission;

import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.BusRouteRepository;
import com.rgcet.admission.repository.CertificateRepository;
import com.rgcet.admission.repository.DepartmentRepository;
import com.rgcet.admission.repository.HostelRepository;
import com.rgcet.admission.repository.ProgramRepository;
import com.rgcet.admission.repository.TuitionFeeStructureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AdmissionApplicationTests {

    @Autowired
    private AdmissionCategoryRepository categoryRepository;
    @Autowired
    private ProgramRepository programRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private HostelRepository hostelRepository;
    @Autowired
    private BusRouteRepository busRouteRepository;
    @Autowired
    private TuitionFeeStructureRepository feeStructureRepository;

    @Test
    void contextLoadsAndSeedsMasterData() {
        assertEquals(2, categoryRepository.count());
        assertEquals(3, programRepository.count());
        assertEquals(10, departmentRepository.count());
        assertEquals(12, certificateRepository.count());
        assertEquals(1, hostelRepository.count());
        assertEquals(6, busRouteRepository.count());
        assertTrue(feeStructureRepository.count() > 0);
    }
}
