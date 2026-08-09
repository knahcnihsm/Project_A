package com.rgcet.admission.service;

import com.rgcet.admission.dto.BulkUpdateDtos.BulkUpdateSchemaDto;
import com.rgcet.admission.dto.BulkUpdateDtos.ColumnDto;
import com.rgcet.admission.dto.BulkUpdateDtos.TableDto;
import com.rgcet.admission.entity.AddressType;
import com.rgcet.admission.entity.Caste;
import com.rgcet.admission.entity.Gender;
import com.rgcet.admission.repository.AdmissionCategoryRepository;
import com.rgcet.admission.repository.BusRouteRepository;
import com.rgcet.admission.repository.CertificateRepository;
import com.rgcet.admission.repository.DepartmentRepository;
import com.rgcet.admission.repository.HostelRepository;
import com.rgcet.admission.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BulkUpdateSchemaService {

    public static final String TYPE_KEY = "KEY";
    public static final String TYPE_STRING = "STRING";
    public static final String TYPE_ENUM = "ENUM";
    public static final String TYPE_NUMBER = "NUMBER";
    public static final String TYPE_DATE = "DATE";
    public static final String TYPE_BOOLEAN = "BOOLEAN";
    public static final String TYPE_REFERENCE = "REFERENCE";

    public static final String LOOKUP_KEY = "application_no";
    public static final String UPDATED_BY_DEFAULT = "Admin";

    private final AdmissionCategoryRepository categoryRepository;
    private final ProgramRepository programRepository;
    private final DepartmentRepository departmentRepository;
    private final CertificateRepository certificateRepository;
    private final HostelRepository hostelRepository;
    private final BusRouteRepository busRouteRepository;

    public BulkUpdateSchemaDto getSchema() {
        return new BulkUpdateSchemaDto(
                LOOKUP_KEY,
                UPDATED_BY_DEFAULT,
                buildTables(),
                buildMasterData());
    }

    public Optional<TableDto> findTable(String tableName) {
        if (tableName == null) {
            return Optional.empty();
        }
        return buildTables().stream()
                .filter(t -> t.tableName().equalsIgnoreCase(tableName.trim()))
                .findFirst();
    }

    private List<TableDto> buildTables() {
        List<TableDto> tables = new ArrayList<>();
        tables.add(new TableDto("student_details", List.of(
                key(),
                col("register_no", TYPE_STRING, false, null, null),
                col("student_name", TYPE_STRING, false, null, null),
                col("date_of_birth", TYPE_DATE, false, null, null),
                col("aadhaar_no", TYPE_STRING, false, null, null),
                enumCol("gender", Gender.class),
                col("district", TYPE_STRING, false, null, null),
                col("nationality", TYPE_STRING, false, null, null),
                enumCol("caste", Caste.class),
                col("mobile_number", TYPE_STRING, false, null, null),
                col("email_id", TYPE_STRING, false, null, null))));

        tables.add(new TableDto("parent_details", List.of(
                key(),
                col("father_name", TYPE_STRING, false, null, null),
                col("father_mobile_no", TYPE_STRING, false, null, null),
                col("father_occupation", TYPE_STRING, false, null, null),
                col("annual_income", TYPE_NUMBER, false, null, null))));

        tables.add(new TableDto("admission", List.of(
                key(),
                col("category_id", TYPE_REFERENCE, false, null, "AdmissionCategory"),
                col("program_id", TYPE_REFERENCE, false, null, "Program"),
                col("department_id", TYPE_REFERENCE, false, null, "Department"),
                col("batch", TYPE_STRING, false, null, null),
                col("date_of_admission", TYPE_DATE, false, null, null))));

        tables.add(new TableDto("address", List.of(
                key(),
                enumCol("address_type", AddressType.class),
                col("address_line", TYPE_STRING, false, null, null),
                col("pincode", TYPE_STRING, false, null, null),
                col("phone", TYPE_STRING, false, null, null),
                col("mobile", TYPE_STRING, false, null, null),
                col("email", TYPE_STRING, false, null, null),
                col("same_as_permanent", TYPE_BOOLEAN, false, null, null))));

        tables.add(new TableDto("qualifying_examination", List.of(
                key(),
                col("institution_name", TYPE_STRING, false, null, null),
                col("institution_place", TYPE_STRING, false, null, null),
                col("exam_passed", TYPE_STRING, false, null, null),
                col("month_year_of_passing", TYPE_STRING, false, null, null),
                col("sslc_registration_no", TYPE_STRING, false, null, null),
                col("sslc_percentage", TYPE_NUMBER, false, null, null),
                col("hsc_registration_no", TYPE_STRING, false, null, null),
                col("hsc_percentage", TYPE_NUMBER, false, null, null))));

        tables.add(new TableDto("diploma_details", List.of(
                key(),
                col("diploma", TYPE_STRING, false, null, null),
                col("institution_name", TYPE_STRING, false, null, null),
                col("board", TYPE_STRING, false, null, null),
                col("second_year_percentage", TYPE_NUMBER, false, null, null),
                col("third_year_percentage", TYPE_NUMBER, false, null, null),
                col("aggregate_percentage", TYPE_NUMBER, false, null, null))));

        tables.add(new TableDto("pg_qualification", List.of(
                key(),
                col("university_name", TYPE_STRING, false, null, null),
                col("university_place", TYPE_STRING, false, null, null),
                col("institution_name", TYPE_STRING, false, null, null),
                col("institution_place", TYPE_STRING, false, null, null),
                col("exam_passed", TYPE_STRING, false, null, null),
                col("month_year_of_passing", TYPE_STRING, false, null, null),
                col("total_percentage", TYPE_NUMBER, false, null, null),
                col("main_subject_percentage", TYPE_NUMBER, false, null, null),
                col("degree_registration_no", TYPE_STRING, false, null, null))));

        tables.add(new TableDto("student_fee", List.of(
                key(),
                col("cut_off_mark", TYPE_NUMBER, false, null, null),
                col("merit_percent", TYPE_NUMBER, false, null, null),
                col("original_tuition_fee", TYPE_NUMBER, false, null, null),
                col("scholarship_amount", TYPE_NUMBER, false, null, null),
                col("tuition_fee_per_year", TYPE_NUMBER, false, null, null),
                col("course_duration_years", TYPE_NUMBER, false, null, null),
                col("total_tuition_fee", TYPE_NUMBER, false, null, null),
                col("bus_required", TYPE_BOOLEAN, false, null, null),
                col("route_id", TYPE_REFERENCE, false, null, "BusRoute"),
                col("bus_stop_id", TYPE_REFERENCE, false, null, "BusStop"),
                col("bus_fee", TYPE_NUMBER, false, null, null),
                col("hostel_required", TYPE_BOOLEAN, false, null, null),
                col("hostel_id", TYPE_REFERENCE, false, null, "Hostel"),
                col("hostel_fee", TYPE_NUMBER, false, null, null),
                col("paid_amount", TYPE_NUMBER, false, null, null),
                col("pending_amount", TYPE_NUMBER, false, null, null))));

        tables.add(new TableDto("student_certificate", List.of(
                key(),
                col("certificate_id", TYPE_REFERENCE, true, null, "Certificate"),
                col("is_submitted", TYPE_BOOLEAN, false, null, null),
                col("file_path", TYPE_STRING, false, null, null))));

        return tables;
    }

    private Map<String, List<String>> buildMasterData() {
        Map<String, List<String>> masterData = new LinkedHashMap<>();
        masterData.put("AdmissionCategory", stringList(() -> categoryRepository.findAll().stream()
                .map(c -> c.getCategoryName()).toList()));
        masterData.put("Program", stringList(() -> programRepository.findAll().stream()
                .map(p -> p.getProgramName()).toList()));
        masterData.put("Department", stringList(() -> departmentRepository.findAll().stream()
                .map(d -> d.getDepartmentName()).toList()));
        masterData.put("Certificate", stringList(() -> certificateRepository.findAll().stream()
                .map(c -> c.getCertificateName()).toList()));
        masterData.put("Hostel", stringList(() -> hostelRepository.findAll().stream()
                .map(h -> String.valueOf(h.getHostelId())).toList()));
        masterData.put("BusRoute", stringList(() -> busRouteRepository.findAll().stream()
                .map(r -> r.getRouteName()).toList()));
        masterData.put("BusStop", stringList(() -> busRouteRepository.findAll().stream()
                .flatMap(r -> r.getStops().stream())
                .map(s -> s.getStopName()).toList()));
        return masterData;
    }

    private List<String> stringList(Supplier<List<String>> supplier) {
        return new ArrayList<>(supplier.get());
    }

    private static ColumnDto key() {
        return new ColumnDto(LOOKUP_KEY, TYPE_KEY, true, List.of(), null, true);
    }

    private static ColumnDto col(String name, String type, boolean required, Object ignored, String fkReference) {
        return new ColumnDto(name, type, required, List.of(), fkReference, false);
    }

    private static ColumnDto enumCol(String name, Class<? extends Enum<?>> enumClass) {
        List<String> values = new ArrayList<>();
        for (Enum<?> e : enumClass.getEnumConstants()) {
            values.add(e.name());
        }
        return new ColumnDto(name, TYPE_ENUM, false, values, null, false);
    }
}
