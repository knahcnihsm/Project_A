# Academic ERP Admission Portal – Frontend Workflow

---

## Login

```
Login
   │
   ▼
Dashboard
   │
   ├─────────────────────────────────────────────┐
   │                                             │
   ▼                                             ▼
Add New Admission                        Student List
   │                                             │
   │                                             ├── View Student
   │                                             ├── Edit Student
   │                                             ├── Archive Student
   │                                             └── Export
   │
   ▼
Student Details
   │
   ▼
Parent Details
   │
   ▼
Communication
   │
   ▼
Academic Details
   │
   ├───────────────┬────────────────────┬───────────────────┐
   │               │                    │
   ▼               ▼                    ▼
First Year      II Year              PG
B.Tech          Lateral              M.Tech/MBA/MCA
   │               │                    │
   ▼               ▼                    ▼
Qualifying      Diploma             PG Qualification
Examination     Details
   │
   ▼
HSC Marks
(Academic / Vocational)
   │
   ▼
Fee Structure
   │
   ▼
Certificates Upload
   │
   ▼
Save Admission
   │
   ▼
Student List
```

---

# Dashboard

The Dashboard is the landing page after login.

## Components

### Top Navigation

- College Logo
- Admission Portal
- Academic ERP System
- Dark Mode Toggle
- Settings
- Admin User Profile

---

### Sidebar

- Dashboard
- Add New Admission
- Edit Student Details
- Archived Student Details
- Export
  - Export Selected Students (Excel)
  - Export Searched Students (Excel)
  - Export Student Details (PDF)

---

### Main Content

- Search Student
- Filter
- Student Admission List
- Pagination

### Actions

- View
- Edit
- More Menu

---

# Add New Admission

The Add New Admission module is a **multi-step admission wizard**.

The sidebar changes into a **step navigation menu**.

```
Student Details

↓

Parent Details

↓

Communication

↓

Academic Details

↓

Fee Structure

↓

Certificates Upload

↓

Save Admission
```

---

# Student Details

## Fields

- Application Number
- Register Number
- Student Name
- Date of Birth
- Age (Auto Calculate)
- Aadhaar Number
- Gender
- District
- Nationality
- Caste

### Buttons

- Previous
- Save & Next

---

# Parent Details

## Fields

- Father Name
- Father Mobile Number
- Father Occupation
- Annual Income

### Buttons

- Previous
- Save & Next

---

# Communication

## Permanent Address

- Address
- PIN Code
- Phone
- Mobile
- Email

## Communication Address

- Address
- PIN Code
- Phone
- Mobile
- Email

### Feature

- ☑ Same as Permanent Address

### Buttons

- Previous
- Save & Next

---

# Academic Details

## Common Fields

- Admission Category
- Program
- Department
- Batch
- Date of Admission

After Program selection, the workflow changes automatically.

---

# Program Selection Logic

## If Program = First Year B.Tech

Sidebar becomes

```
Academic Details

▼

Admission Details

Qualifying Examination

HSC Marks
```

---

## Qualifying Examination

### Fields

- Institution Name
- Institution Place
- Examination Passed
- Month & Year of Passing
- SSLC Percentage
- SSLC Register Number
- HSC Percentage
- HSC Register Number

---

## HSC Marks

### Tabs

- Academic Marks
- Vocational Marks

### Academic Subjects

- Maths
- Physics
- Chemistry
- Computer Science
- Biology
- Bio Technology

### Vocational Subjects

- Related Subject I
- Related Subject II
- Vocational Subject
- Theory
- Practical I
- Practical II

### Automatic Calculations

- Subject Percentage
- Overall Percentage
- Engineering Cut-Off

### Summary

- Total Marks
- Overall Percentage
- Engineering Cut-Off

---

## If Program = II Year B.Tech (Lateral Entry)

Sidebar becomes

```
Academic Details

▼

Admission Details

Diploma Details
```

---

## Diploma Details

### Fields

- Diploma Course
- Institution Name
- Board
- Month & Year
- Register Number

### Table

- Diploma
- Second Year %
- Third Year %
- Aggregate %

### Summary

- Second Year
- Third Year
- Aggregate

---

## If Program = PG

Applicable for

- M.Tech (CSE)
- M.Tech (Wireless Communication)
- MBA
- MCA

Sidebar becomes

```
Academic Details

▼

Admission Details

PG Qualification
```

---

## PG Qualification

### Fields

- University Name
- University Place
- Institution Name
- Institution Place
- Examination Passed
- Month & Year
- Total Percentage
- Main Subject Percentage
- Degree Registration Number

### Summary

- Examination
- Overall Percentage
- Main Subject Percentage

---

# Fee Structure

## Fields

- Cut-Off Mark
- Tuition Fee Per Year
- Total Tuition Fee

### Bus Section

```
☐ Bus Required

↓

Bus Route

↓

Bus Fee
```

### Hostel Section

```
☐ Hostel Required

↓

Hostel Fee
```

### Summary

- Tuition Fee
- Bus Fee
- Hostel Fee
- Grand Total

### Buttons

- Previous
- Save & Next

---

# Certificates Upload

## Table Columns

- Received
- Certificate Name
- Upload
- Preview
- Status

## Certificates

- Provisional Allotment Order
- Special Category Certificate
- Provisional Certificate
- Undertaking Form
- Mark Sheet
- Degree Certificate
- Residence Certificate
- Transfer Certificate
- Proof of Age
- Community Certificate
- Conduct Certificate
- Aadhaar Card

### Features

- Checkbox
- Upload
- Replace
- Remove
- Preview

### Summary

- Uploaded
- Pending
- Progress Bar

### Buttons

- Previous
- Save Admission

---

# Student List

## Features

- Search
- Filter
- Pagination
- Checkbox Selection

## Actions

- View
- Edit
- Archive

---

# View Student

Opens the complete admission details in **Read Only Mode**.

Displays

- Student Details
- Parent Details
- Communication
- Academic Details
- Fee Structure
- Certificates

---

# Edit Student

There are three workflows.

## 1. One Student Selected

```
Student Selected

↓

Edit Student Details

↓

Retrieve Student Data

↓

Update

↓

Save

↓

Dashboard
```

The same **Add New Admission** page is reused.

Only the page heading changes to:

**Edit Student Details**

---

## 2. No Student Selected

```
Edit Student Details

↓

Open Modal

↓

Upload Excel

↓

Validate

↓

Update Existing Students

↓

Success
```

---

## 3. Multiple Students Selected

```
Show Warning

↓

Please select only one student.
```

---

# Archive Student

```
Student List

↓

Archive

↓

Confirmation Dialog

↓

Select Reason

↓

Archive Student

↓

Archived Student Details
```

### Archive Reasons

- TC
- Admission Cancelled
- Duplicate Admission
- Student Request
- Other

---

# Archived Student Details

Displays all archived students.

## Table

- Application Number
- Register Number
- Student Name
- Department
- Program
- Reason
- Deleted By
- Deleted Date

### Actions

- View
- Restore

---

# Restore Student

```
Archived Student

↓

Restore

↓

Confirmation

↓

Move back to Active Student List

↓

Dashboard
```

---

# Export

Three export options

```
Export

├── Export Selected Students (Excel)

├── Export Searched Students (Excel)

└── Export Student Details (PDF)
```

---

# Shared Layout Components

Every page uses the same layout.

- Top Navigation Bar
- Left Sidebar
- Breadcrumb
- Main Content Area
- Footer

---

# Shared UI Components

- App Button
- App Card
- App Input
- App Select
- App Date Picker
- App Checkbox
- App Table
- App Tabs
- App Upload
- App Modal
- App Snackbar
- Confirmation Dialog
- Progress Bar
- Summary Card
- Search Bar
- Filter Dropdown
- Pagination

---

# Dynamic Frontend Logic

- Age is automatically calculated from Date of Birth.
- Academic sidebar sub-menus appear only after a Program is selected.
- Department dropdown updates based on the selected Program.
- HSC, Diploma, or PG sections are displayed dynamically based on the selected Program.
- Bus Route dropdown is enabled only when **Bus Required** is checked.
- Hostel Fee is applied only when **Hostel Required** is checked.
- Tuition Fee, Total Fee, Grand Total, and Engineering Cut-Off are calculated automatically.
- Communication Address can be copied from Permanent Address using the checkbox.
- All uploaded certificates display their current upload status.
- The same admission form is reused for **Add**, **Edit**, and **View**, with only the page mode (Editable / Read Only) changing.
