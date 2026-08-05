academic-erp-admission-portal/
│
├── public/
│ ├── favicon.ico
│ ├── logo.png
│ └── images/
│ ├── college-logo.png
│ ├── sidebar-building.png
│ └── empty-state.svg
│
├── src/
│
├── assets/
│ ├── icons/
│ ├── images/
│ ├── fonts/
│ └── styles/
│
├── components/
│ │
│ ├── common/
│ │ ├── Loading.tsx
│ │ ├── PageHeader.tsx
│ │ ├── SearchBar.tsx
│ │ ├── FilterBar.tsx
│ │ ├── EmptyState.tsx
│ │ ├── ConfirmDialog.tsx
│ │ ├── Snackbar.tsx
│ │ └── NotFound.tsx
│ │
│ ├── layout/
│ │ ├── Header.tsx
│ │ ├── Sidebar.tsx
│ │ ├── Footer.tsx
│ │ ├── Breadcrumb.tsx
│ │ └── MainLayout.tsx
│ │
│ ├── ui/
│ │ ├── AppButton.tsx
│ │ ├── AppCard.tsx
│ │ ├── AppInput.tsx
│ │ ├── AppSelect.tsx
│ │ ├── AppDatePicker.tsx
│ │ ├── AppCheckbox.tsx
│ │ ├── AppTable.tsx
│ │ ├── AppModal.tsx
│ │ ├── AppStepper.tsx
│ │ ├── AppUpload.tsx
│ │ └── SummaryCard.tsx
│ │
│ └── dashboard/
│ ├── StatisticsCards.tsx
│ ├── StudentTable.tsx
│ └── ExportMenu.tsx
│
├── pages/
│ │
│ ├── dashboard/
│ │ └── Dashboard.tsx
│ │
│ ├── admission/
│ │ ├── AddAdmission.tsx
│ │ ├── StudentDetails.tsx
│ │ ├── ParentDetails.tsx
│ │ ├── Communication.tsx
│ │ ├── AcademicDetails.tsx
│ │ ├── QualifyingExamination.tsx
│ │ ├── HSCMarks.tsx
│ │ ├── DiplomaDetails.tsx
│ │ ├── PGQualification.tsx
│ │ ├── FeeStructure.tsx
│ │ └── CertificatesUpload.tsx
│ │
│ ├── student/
│ │ ├── StudentList.tsx
│ │ ├── EditStudent.tsx
│ │ ├── ViewStudent.tsx
│ │ └── BulkUpdate.tsx
│ │
│ ├── archive/
│ │ ├── ArchivedStudents.tsx
│ │ └── ArchivedStudentView.tsx
│ │
│ ├── export/
│ │ └── ExportPage.tsx
│ │
│ ├── settings/
│ │ └── Settings.tsx
│ │
│ └── profile/
│ └── Profile.tsx
│
├── forms/
│ ├── StudentForm.ts
│ ├── ParentForm.ts
│ ├── CommunicationForm.ts
│ ├── AcademicForm.ts
│ ├── FeeForm.ts
│ └── CertificateForm.ts
│
├── hooks/
│ ├── useTheme.ts
│ ├── useSnackbar.ts
│ ├── useDialog.ts
│ ├── useStudent.ts
│ └── useUpload.ts
│
├── services/
│ ├── api.ts
│ ├── student.service.ts
│ ├── admission.service.ts
│ ├── academic.service.ts
│ ├── fee.service.ts
│ ├── certificate.service.ts
│ ├── archive.service.ts
│ ├── export.service.ts
│ ├── department.service.ts
│ ├── program.service.ts
│ └── auth.service.ts
│
├── context/
│ ├── ThemeContext.tsx
│ ├── AuthContext.tsx
│ └── AdmissionContext.tsx
│
├── routes/
│ ├── AppRoutes.tsx
│ ├── ProtectedRoute.tsx
│ └── RoutePaths.ts
│
├── types/
│ ├── student.ts
│ ├── parent.ts
│ ├── academic.ts
│ ├── fee.ts
│ ├── certificate.ts
│ ├── department.ts
│ └── common.ts
│
├── utils/
│ ├── constants.ts
│ ├── validators.ts
│ ├── dateUtils.ts
│ ├── feeCalculator.ts
│ ├── cutoffCalculator.ts
│ ├── exportExcel.ts
│ └── exportPdf.ts
│
├── theme/
│ ├── colors.ts
│ ├── typography.ts
│ ├── shadows.ts
│ ├── lightTheme.ts
│ ├── darkTheme.ts
│ └── index.ts
│
├── schemas/
│ ├── student.schema.ts
│ ├── parent.schema.ts
│ ├── communication.schema.ts
│ ├── academic.schema.ts
│ ├── fee.schema.ts
│ └── certificate.schema.ts
│
├── App.tsx
├── main.tsx
├── vite.config.ts
└── tsconfig.json
