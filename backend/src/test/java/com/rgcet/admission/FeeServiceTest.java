package com.rgcet.admission;

import com.rgcet.admission.entity.AdmissionCategory;
import com.rgcet.admission.entity.Department;
import com.rgcet.admission.entity.Program;
import com.rgcet.admission.entity.TuitionFeeStructure;
import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.BusRouteRepository;
import com.rgcet.admission.repository.BusStopRepository;
import com.rgcet.admission.repository.HostelRepository;
import com.rgcet.admission.repository.TuitionFeeStructureRepository;
import com.rgcet.admission.service.FeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeeServiceTest {

    private TuitionFeeStructureRepository feeRepo;
    private AdmissionCategoryRepository categoryRepo;
    private FeeService feeService;

    @BeforeEach
    void setUp() {
        feeRepo = mock(TuitionFeeStructureRepository.class);
        categoryRepo = mock(AdmissionCategoryRepository.class);
        feeService = new FeeService(feeRepo, categoryRepo,
                mock(BusRouteRepository.class), mock(BusStopRepository.class), mock(HostelRepository.class));
    }

    private TuitionFeeStructure structure(BigDecimal min, BigDecimal max, int fee) {
        TuitionFeeStructure s = new TuitionFeeStructure();
        s.setMinimumPercentage(min);
        s.setMaximumPercentage(max);
        s.setTuitionFee(BigDecimal.valueOf(fee));
        return s;
    }

    @Test
    void lookupPicksBandContainingMerit() {
        List<TuitionFeeStructure> rows = List.of(
                structure(new BigDecimal("0"), new BigDecimal("60"), 100000),
                structure(new BigDecimal("60"), new BigDecimal("80"), 90000),
                structure(new BigDecimal("80"), new BigDecimal("100"), 80000));

        assertEquals(100000, FeeService.lookupBand(rows, new BigDecimal("20")).getTuitionFee().intValue());
        // 60 falls into the 60-80 band ([min, max))
        assertEquals(90000, FeeService.lookupBand(rows, new BigDecimal("60")).getTuitionFee().intValue());
        assertEquals(90000, FeeService.lookupBand(rows, new BigDecimal("79.99")).getTuitionFee().intValue());
        assertEquals(80000, FeeService.lookupBand(rows, new BigDecimal("80")).getTuitionFee().intValue());
        // 100 belongs to the top band
        assertEquals(80000, FeeService.lookupBand(rows, new BigDecimal("100")).getTuitionFee().intValue());
    }

    @Test
    void lookupWithNullMeritPrefersFlatRow() {
        List<TuitionFeeStructure> rows = List.of(
                structure(null, null, 100000),
                structure(new BigDecimal("80"), new BigDecimal("100"), 80000));
        assertEquals(100000, FeeService.lookupBand(rows, null).getTuitionFee().intValue());
    }

    @Test
    void lookupFallsBackToManagementDefaultWhenNoBandMatches() {
        Program program = new Program();
        Department department = new Department();
        AdmissionCategory centac = new AdmissionCategory();
        centac.setCategoryName("CENTAC");
        AdmissionCategory management = new AdmissionCategory();
        management.setCategoryName("Management");

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, centac))
                .thenReturn(List.of());
        when(categoryRepo.findByCategoryNameIgnoreCase("Management")).thenReturn(Optional.of(management));
        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, management))
                .thenReturn(List.of(structure(null, null, 100000)));

        TuitionFeeStructure result = feeService.lookup(program, department, centac, new BigDecimal("25"));
        assertEquals(100000, result.getTuitionFee().intValue());
    }

    @Test
    void lookupReturnsNullWhenNothingMatches() {
        Program program = new Program();
        Department department = new Department();
        AdmissionCategory centac = new AdmissionCategory();
        centac.setCategoryName("CENTAC");

        when(feeRepo.findByProgramAndDepartmentAndCategory(program, department, centac))
                .thenReturn(List.of());
        when(categoryRepo.findByCategoryNameIgnoreCase("Management")).thenReturn(Optional.empty());

        assertNull(feeService.lookup(program, department, centac, new BigDecimal("25")));
    }
}
