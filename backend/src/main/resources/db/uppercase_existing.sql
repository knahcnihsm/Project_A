-- One-time migration: uppercase all student-entered text + master reference names
-- Run once against PostgreSQL (erp_admission). Idempotent: re-running is harmless.

UPDATE student_details SET application_no = UPPER(application_no), register_no = UPPER(register_no), student_name = UPPER(student_name), aadhaar_no = UPPER(aadhaar_no), mobile_number = UPPER(mobile_number), email_id = UPPER(email_id), district = UPPER(district), nationality = UPPER(nationality), archive_reason = UPPER(archive_reason);

UPDATE parent_details SET father_name = UPPER(father_name), father_mobile_no = UPPER(father_mobile_no), father_occupation = UPPER(father_occupation);

UPDATE address SET address_line = UPPER(address_line), pincode = UPPER(pincode), phone = UPPER(phone), mobile = UPPER(mobile), email = UPPER(email);

UPDATE admission SET batch = UPPER(batch);

UPDATE qualifying_examination SET institution_name = UPPER(institution_name), institution_place = UPPER(institution_place), exam_passed = UPPER(exam_passed), month_year_of_passing = UPPER(month_year_of_passing), sslc_registration_no = UPPER(sslc_registration_no), hsc_registration_no = UPPER(hsc_registration_no);
UPDATE hsc_academic_marks SET subject_name = UPPER(subject_name), month_year = UPPER(month_year);
UPDATE hsc_vocational_marks SET subject_name = UPPER(subject_name), month_year = UPPER(month_year);

UPDATE diploma_details SET diploma = UPPER(diploma), institution_name = UPPER(institution_name), board = UPPER(board);
UPDATE pg_qualification SET university_name = UPPER(university_name), university_place = UPPER(university_place), institution_name = UPPER(institution_name), institution_place = UPPER(institution_place), exam_passed = UPPER(exam_passed), month_year_of_passing = UPPER(month_year_of_passing), degree_registration_no = UPPER(degree_registration_no);

UPDATE archive SET archive_reason = UPPER(archive_reason), description = UPPER(description);

UPDATE department SET department_name = UPPER(department_name);
UPDATE program SET program_name = UPPER(program_name);
UPDATE admission_category SET category_name = UPPER(category_name);
UPDATE bus_route SET route_name = UPPER(route_name);
UPDATE bus_stop SET stop_name = UPPER(stop_name);
UPDATE certificate SET certificate_name = UPPER(certificate_name);
