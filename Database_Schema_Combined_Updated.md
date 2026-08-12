# Database Schema

## Address

```
Address
--------------------------
address_id (PK)

student_id (FK)

permanent_address
communication_address
same_as_permanent

pincode
phone
mobile
email
```

## Admission

```
Admission
--------------------------
admission_id (PK)

student_id (FK)

category_id (FK)
program_id (FK)
department_id (FK)

batch
date_of_admission
```

## Admission_Category

```
Admission_Category
--------------------------
category_id (PK)

category_name
```

## Archive

```
Archive
--------------------------
archive_id (PK)

student_id (FK)

archive_reason

description

archived_date
```

## Bus_Route

```
Bus_Route
--------------------------
route_id (PK)

route_name

bus_fee
```



## Bus_Stop

```
Bus_Stop
--------------------------
bus_stop_id (PK)

route_id (FK)

stop_order

stop_name

transport_fee
```

## Bus Module Relationship

```text
Bus_Route (1) --------< (M) Bus_Stop
Bus_Route (1) --------< (M) Student_Details
Bus_Stop  (1) --------< (M) Student_Details
```

## Certificate

```
Certificate
--------------------------
certificate_id (PK)

certificate_name
```

## Department

```
Department
--------------------------
department_id (PK)

department_name
```

## Diploma_Details

```
Diploma_Details
--------------------------
diploma_id (PK)

student_id (FK)

diploma
board

second_year_percentage

third_year_percentagev
```

## HSC_Academic_Marks

```
HSC_Academic_Marks
--------------------------
academic_mark_id (PK)

qualification_id (FK)

subject_name
month_year

maximum_marks
marks_obtained

percentage
```

## HSC_Vocational_Marks

```
HSC_Vocational_Marks
--------------------------
vocational_mark_id (PK)

qualification_id (FK)

subject_name
month_year

maximum_marks
marks_obtained

percentage
```

Canonical subject rows per vocational student (5 rows):

- VOCATIONAL SUBJECT THEORY
- RELATED SUBJECT I
- RELATED SUBJECT II
- RELATED SUBJECT II PRACTICAL I  (sub-row of Related Subject II)
- RELATED SUBJECT II PRACTICAL II (sub-row of Related Subject II)

Note: Practical sub-rows are excluded from the engineering cut-off (Vocational Subject Theory + Related Subject I + Related Subject II = 300) but count toward total marks / overall percentage.

## Hostel

```
Hostel
--------------------------
hostel_id (PK)

hostel_fee
```

## PG_Qualification

```
PG_Qualification
--------------------------
pg_id (PK)

student_id (FK)

university_name
university_place

institution_name
institution_place

exam_passed

month_year_of_passing

total_percentage

main_subject_percentage

degree_registration_no
```

## Parent_Details

```
Parent_Details
--------------------------
parent_id (PK)

student_id (FK)

father_name
father_mobile_no
father_occupation
annual_income
```

## Program

```
Program
--------------------------
program_id (PK)

program_name
```

## Qualifying_Examination

```
Qualifying_Examination
--------------------------
qualification_id (PK)

student_id (FK)

institution_name
institution_place

exam_passed

month_year_of_passing

sslc_registration_no
sslc_percentage

hsc_registration_no
hsc_percentage
```

## Student_Certificate

```
Student_Certificate
--------------------------
student_certificate_id (PK)

student_id (FK)

certificate_id (FK)

is_submitted

file_path
```

## Student_Details

```
Student_Details
--------------------------
student_id (PK)

application_no
register_no
student_name
date_of_birth
age (Derived)
aadhaar_no
gender
district
nationality
caste
```

## Student_Fee

```
Student_Fee
--------------------------
student_fee_id (PK)

student_id (FK)

fee_structure_id (FK)

hostel_id (FK)

route_id (FK)

total_fee (Derived)
```

## Tuition_Fee_Structure

```
Tuition_Fee_Structure
--------------------------
fee_structure_id (PK)

program_id (FK)

department_id (FK)

category_id (FK)

minimum_percentage

maximum_percentage

tuition_fee
```

