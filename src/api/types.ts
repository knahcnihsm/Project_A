export type Gender = 'MALE' | 'FEMALE' | 'TRANSGENDER';
export type Caste = 'OC' | 'BC' | 'BCM' | 'MBC' | 'SC' | 'SCA' | 'ST';
export type StudentStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED';
export type PaymentStatus = 'PAID' | 'PARTIAL' | 'PENDING';

export interface PersonalStepRequest {
  applicationNumber: string;
  registerNumber: string;
  studentName: string;
  dateOfBirth: string;
  aadhaarNumber?: string;
  gender?: Gender | null;
  district?: string;
  nationality?: string;
  caste?: Caste | null;
}

export interface ParentStepRequest {
  fatherName: string;
  fatherMobile?: string;
  fatherOccupation?: string;
  annualIncome?: number;
}

export interface AddressRequest {
  addressLine?: string;
  pincode?: string;
  phone?: string;
  mobile?: string;
  email?: string;
}

export interface CommunicationStepRequest {
  permanentAddress: AddressRequest;
  communicationAddress: AddressRequest;
  sameAsPermanent: boolean;
}

export interface AcademicStepRequest {
  categoryId: number;
  programId: number;
  departmentId?: number | null;
  batch?: string;
  dateOfAdmission?: string;
}

export interface QualifyingExamStepRequest {
  institutionName?: string;
  institutionPlace?: string;
  examPassed?: string;
  monthYearPassing?: string;
  sslcPercentage?: number;
  sslcRegisterNumber?: string;
  hscPercentage?: number;
  hscRegisterNumber?: string;
}

export interface SubjectMarkRequest {
  subject: string;
  monthYear?: string;
  maxMarks?: number;
  marksObtained?: number;
}

export interface HscMarksStepRequest {
  stream: string;
  academicMarks: SubjectMarkRequest[];
  vocationalMarks: SubjectMarkRequest[];
}

export interface DiplomaStepRequest {
  diplomaCourse?: string;
  institutionName?: string;
  board?: string;
  secondYearPercentage?: number;
  thirdYearPercentage?: number;
  aggregatePercentage?: number;
}

export interface PgStepRequest {
  universityName?: string;
  universityPlace?: string;
  institutionName?: string;
  institutionPlace?: string;
  examPassed?: string;
  monthYearPassing?: string;
  totalPercentage?: number;
  mainSubjectPercentage?: number;
  degreeRegistrationNumber?: string;
}

export interface FeeStepRequest {
  cutOffMark?: number;
  busRequired: boolean;
  routeId?: number | null;
  busStopId?: number | null;
  hostelRequired: boolean;
}

export interface CertificateItemRequest {
  certificateId: number;
  submitted: boolean;
  filePath?: string;
}

export interface CertificatesStepRequest {
  certificates: CertificateItemRequest[];
}

export interface SubmitAdmissionRequest {
  studentId?: number | null;
  personal: PersonalStepRequest;
  parent: ParentStepRequest;
  communication: CommunicationStepRequest;
  academic: AcademicStepRequest;
  qualifyingExam?: QualifyingExamStepRequest;
  hscMarks?: HscMarksStepRequest;
  diploma?: DiplomaStepRequest;
  pg?: PgStepRequest;
  fee?: FeeStepRequest;
  certificates?: CertificatesStepRequest;
}

export interface ArchiveRequest {
  reason: string;
  description?: string;
}

export interface BulkPersonalRowRequest {
  rowNumber: number;
  applicationNo?: string;
  registerNo?: string;
  studentName?: string;
  dateOfBirth?: string;
  gender?: string;
  aadhaarNumber?: string;
  nationality?: string;
  district?: string;
  caste?: string;
}

export interface BulkParentRowRequest {
  rowNumber: number;
  applicationNo?: string;
  registerNo?: string;
  fatherName?: string;
  fatherMobile?: string;
  fatherOccupation?: string;
  annualIncome?: string;
}

export interface BulkAddressRowRequest {
  rowNumber: number;
  applicationNo?: string;
  registerNo?: string;
  addressLine?: string;
  pincode?: string;
  phone?: string;
  mobile?: string;
  email?: string;
}

export interface BulkAcademicRowRequest {
  rowNumber: number;
  applicationNo?: string;
  registerNo?: string;
  admissionCategory?: string;
  program?: string;
  department?: string;
  batch?: string;
  dateOfAdmission?: string;
}

export interface BulkQualifyingExamRowRequest {
  rowNumber: number;
  applicationNo?: string;
  registerNo?: string;
  institutionName?: string;
  institutionPlace?: string;
  examPassed?: string;
  monthYearPassing?: string;
  sslcPercentage?: string;
  sslcRegisterNumber?: string;
  hscPercentage?: string;
  hscRegisterNumber?: string;
}

export interface BulkSubjectRowRequest {
  subject?: string;
  monthYear?: string;
  maxMarks?: string;
  marksObtained?: string;
}

export interface BulkHscMarksRowRequest {
  rowNumber: number;
  applicationNo?: string;
  registerNo?: string;
  stream?: string;
  academicMarks: BulkSubjectRowRequest[];
  vocationalMarks: BulkSubjectRowRequest[];
}

export interface BulkDiplomaRowRequest {
  rowNumber: number;
  applicationNo?: string;
  registerNo?: string;
  diplomaCourse?: string;
  institutionName?: string;
  board?: string;
  secondYearPercentage?: string;
  thirdYearPercentage?: string;
  aggregatePercentage?: string;
}

export interface BulkPgRowRequest {
  rowNumber: number;
  applicationNo?: string;
  registerNo?: string;
  universityName?: string;
  universityPlace?: string;
  institutionName?: string;
  institutionPlace?: string;
  examPassed?: string;
  monthYearPassing?: string;
  totalPercentage?: string;
  mainSubjectPercentage?: string;
  degreeRegistrationNumber?: string;
}

export interface BulkWorkbookRequest {
  fileName?: string;
  uploadedBy?: string;
  personal: BulkPersonalRowRequest[];
  parent: BulkParentRowRequest[];
  communication: BulkAddressRowRequest[];
  permanent: BulkAddressRowRequest[];
  academic: BulkAcademicRowRequest[];
  qualifyingExam: BulkQualifyingExamRowRequest[];
  hscMarks: BulkHscMarksRowRequest[];
  diploma: BulkDiplomaRowRequest[];
  pg: BulkPgRowRequest[];
}

export type BulkIssueSeverity = 'ERROR' | 'WARNING';

export interface BulkIssueDto {
  sheet: string;
  rowNumber: number;
  field: string;
  message: string;
  severity: BulkIssueSeverity;
}

export interface FieldChangeDto {
  sheet: string;
  field: string;
  oldValue?: string;
  newValue?: string;
}

export interface BulkPreviewRowDto {
  studentId: number;
  applicationNo?: string;
  registerNo?: string;
  studentName?: string;
  changes: FieldChangeDto[];
}

export interface BulkValidationResponse {
  fileName?: string;
  totalRows: number;
  matchedStudents: number;
  unmatchedRows: number;
  errorCount: number;
  warningCount: number;
  valid: boolean;
  issues: BulkIssueDto[];
  preview: BulkPreviewRowDto[];
}

export interface BulkCommitResponse {
  fileName?: string;
  uploadedBy?: string;
  uploadedAt?: string;
  totalRows: number;
  validRows: number;
  updatedStudents: number;
  noChangeRows: number;
  skippedRows: number;
  failedRows: number;
  warningCount: number;
  durationMs: number;
  status: 'SUCCESS' | 'FAILED';
  issues: BulkIssueDto[];
}

export interface SubjectMarkDto {
  subject: string;
  monthYear?: string;
  maxMarks?: number;
  marksObtained?: number;
  percentage?: number;
}

export interface CertificateDto {
  certificateId?: number;
  name?: string;
  submitted: boolean;
  filePath?: string;
  uploadedAt?: string;
}

export interface StudentResponseDto {
  id: number;
  applicationNumber?: string;
  registerNumber?: string;
  studentName?: string;
  dateOfBirth?: string;
  age?: number;
  aadhaarNumber?: string;
  gender?: Gender;
  district?: string;
  nationality?: string;
  caste?: Caste;
  status: StudentStatus;
  createdAt?: string;
  updatedAt?: string;
  archivedAt?: string;
  archiveReason?: string;
  parent?: {
    fatherName?: string;
    fatherMobile?: string;
    fatherOccupation?: string;
    annualIncome?: number;
  };
  communication?: {
    permanentAddress?: {
      addressLine?: string;
      pincode?: string;
      phone?: string;
      mobile?: string;
      email?: string;
    };
    communicationAddress?: {
      addressLine?: string;
      pincode?: string;
      phone?: string;
      mobile?: string;
      email?: string;
    };
    sameAsPermanent?: boolean;
  };
  academic?: {
    categoryId?: number;
    category?: string;
    programId?: number;
    program?: string;
    durationYears?: number;
    departmentId?: number;
    department?: string;
    batch?: string;
    dateOfAdmission?: string;
  };
  qualifyingExam?: {
    institutionName?: string;
    institutionPlace?: string;
    examPassed?: string;
    monthYearPassing?: string;
    sslcPercentage?: number;
    sslcRegisterNumber?: string;
    hscPercentage?: number;
    hscRegisterNumber?: string;
  };
  hscMarks?: {
    stream?: string;
    academicMarks?: SubjectMarkDto[];
    vocationalMarks?: SubjectMarkDto[];
    totalMaxMarks?: number;
    totalMarksObtained?: number;
    overallPercentage?: number;
    engineeringCutOff?: number;
  };
  diplomaDetails?: {
    diplomaCourse?: string;
    institutionName?: string;
    board?: string;
    secondYearPercentage?: number;
    thirdYearPercentage?: number;
    aggregatePercentage?: number;
  };
  pgQualification?: {
    universityName?: string;
    universityPlace?: string;
    institutionName?: string;
    institutionPlace?: string;
    examPassed?: string;
    monthYearPassing?: string;
    totalPercentage?: number;
    mainSubjectPercentage?: number;
    degreeRegistrationNumber?: string;
  };
  fee?: {
    cutOffMark?: number;
    meritPercent?: number;
    originalTuitionFeePerYear?: number;
    scholarshipAmount?: number;
    tuitionFeePerYear?: number;
    courseDurationYears?: number;
    totalTuitionFee?: number;
    busRequired?: boolean;
    routeId?: number;
    routeName?: string;
    busStopId?: number;
    busStopName?: string;
    busFee?: number;
    hostelRequired?: boolean;
    hostelFee?: number;
    totalFee?: number;
    paidAmount?: number;
    pendingAmount?: number;
    paymentStatus?: PaymentStatus;
  };
  certificates?: CertificateDto[];
}

export interface StudentSummaryDto {
  id: number;
  applicationNumber?: string;
  registerNumber?: string;
  studentName?: string;
  program?: string;
  department?: string;
  category?: string;
  batch?: string;
  status: StudentStatus;
  createdAt?: string;
  archivedAt?: string;
  archiveReason?: string;
}

export interface ProgramDto {
  id: number;
  name: string;
  durationYears?: number;
}

export interface DepartmentDto {
  id: number;
  name: string;
}

export interface CategoryDto {
  id: number;
  name: string;
}

export interface BusStopDto {
  id: number;
  order?: number;
  name: string;
  fee?: number;
}

export interface BusRouteDto {
  id: number;
  name: string;
  busFee?: number;
  stops: BusStopDto[];
}

export interface CertificateMasterDto {
  id: number;
  name: string;
}

export interface HostelDto {
  id: number;
  fee?: number;
}

export interface FeeStructureDto {
  id: number;
  program?: string;
  department?: string;
  category?: string;
  min?: number;
  max?: number;
  fee?: number;
}

export interface ScholarshipStructureDto {
  id: number;
  program?: string;
  department?: string;
  category?: string;
  min?: number;
  max?: number;
  scholarshipAmount?: number;
}

export interface PageDto<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface StudentStatsDto {
  active: number;
  archived: number;
  draft: number;
}
