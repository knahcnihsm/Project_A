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

export interface BulkUpdateRowRequest {
  rowNumber?: number;
  applicationNumber?: string;
  registerNumber?: string;
  studentName?: string;
  dateOfBirth?: string;
  gender?: string;
  aadhaarNumber?: string;
  district?: string;
  caste?: string;
  admissionCategory?: string;
  program?: string;
  department?: string;
  batch?: string;
  fatherName?: string;
  fatherMobile?: string;
  mobileNumber?: string;
  email?: string;
  grandTotalFee?: string;
  status?: string;
  archiveReason?: string;
}

export interface BulkUpdateRequest {
  rows: BulkUpdateRowRequest[];
}

export interface BulkUpdateError {
  rowNumber: number;
  registerNumber: string;
  applicationNumber: string;
  reason: string;
}

export interface BulkUpdateResponse {
  totalRows: number;
  updatedCount: number;
  skippedCount: number;
  failedCount: number;
  errors: BulkUpdateError[];
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
