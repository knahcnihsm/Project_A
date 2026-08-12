import {
  AcademicStepRequest,
  ArchiveRequest,
  BulkAdmissionApply,
  BulkAdmissionPreview,
  BulkUpdatePreview,
  BulkUpdateApply,
  BulkUpdateRequest,
  BulkUpdateSchema,
  BusRouteDto,
  CategoryDto,
  CertificateMasterDto,
  CertificatesStepRequest,
  CommunicationStepRequest,
  DepartmentDto,
  DiplomaStepRequest,
  FeeStepRequest,
  FeeStructureDto,
  HostelDto,
  HscMarksStepRequest,
  PageDto,
  ParentStepRequest,
  PgStepRequest,
  PersonalStepRequest,
  ProgramDto,
  QualifyingExamStepRequest,
  ScholarshipStructureDto,
  StudentResponseDto,
  StudentStatsDto,
  StudentSummaryDto,
  AdminProfileDto,
  UpdateAdminProfileRequest,
} from './types';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://localhost:8080';

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      if (typeof body === 'string' && body) {
        message = body;
      } else if (body?.message) {
        message = body.message;
      } else if (Array.isArray(body) && body.length > 0) {
        const first = body[0];
        message =
          first?.defaultMessage || first?.message || JSON.stringify(first);
      }
    } catch {
      // ignore parse errors, fall back to generic message
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

const json = (method: string) => <T>(path: string, body?: unknown) =>
  request<T>(path, {
    method,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

const get = <T>(path: string) => request<T>(path);
const post = json('POST');
const put = json('PUT');

const buildQuery = (params: Record<string, string | number | undefined>): string => {
  const qs = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      qs.set(key, String(value));
    }
  });
  const str = qs.toString();
  return str ? `?${str}` : '';
};

// ---------------- Student API ----------------

export const studentApi = {
  createStudent: (body: PersonalStepRequest) =>
    post<StudentResponseDto>('/api/students', body),

  updatePersonal: (id: number | string, body: PersonalStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/personal`, body),

  updateParent: (id: number | string, body: ParentStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/parent`, body),

  updateCommunication: (id: number | string, body: CommunicationStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/communication`, body),

  updateAcademic: (id: number | string, body: AcademicStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/academic`, body),

  updateQualifyingExam: (id: number | string, body: QualifyingExamStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/qualifying-exam`, body),

  updateHscMarks: (id: number | string, body: HscMarksStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/hsc-marks`, body),

  updateDiploma: (id: number | string, body: DiplomaStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/diploma`, body),

  updatePg: (id: number | string, body: PgStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/pg`, body),

  updateFee: (id: number | string, body: FeeStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/fee`, body),

  updateCertificates: (id: number | string, body: CertificatesStepRequest) =>
    put<StudentResponseDto>(`/api/students/${id}/certificates`, body),

  submitAdmission: (body: SubmitAdmissionRequest) =>
    post<StudentResponseDto>('/api/students/submit', body),

  finalize: (id: number | string) =>
    post<StudentResponseDto>(`/api/students/${id}/finalize`),

  getStudent: (id: number | string) => get<StudentResponseDto>(`/api/students/${id}`),

  listStudents: (params: {
    search?: string;
    departmentId?: number;
    programId?: number;
    categoryId?: number;
    batch?: string;
    status?: StudentStatus;
    page?: number;
    size?: number;
  } = {}) =>
    get<PageDto<StudentResponseDto>>(
      `/api/students/list${buildQuery({ ...params, page: params.page ?? 0, size: params.size ?? 1000 })}`
    ),

  listArchived: () => get<StudentSummaryDto[]>('/api/students/archived'),

  archive: (id: number | string, body: ArchiveRequest) =>
    post<StudentResponseDto>(`/api/students/${id}/archive`, body),

  restore: (id: number | string) => post<StudentResponseDto>(`/api/students/${id}/restore`),

  stats: () => get<StudentStatsDto>('/api/students/stats'),
};

// ---------------- Master data API ----------------

export const masterDataApi = {
  programs: () => get<ProgramDto[]>('/api/programs'),
  departmentsByProgram: (id: number) => get<DepartmentDto[]>(`/api/programs/${id}/departments`),
  departments: () => get<DepartmentDto[]>('/api/departments'),
  categories: () => get<CategoryDto[]>('/api/categories'),
  certificates: () => get<CertificateMasterDto[]>('/api/certificates'),
  hostels: () => get<HostelDto[]>('/api/hostels'),
  busRoutes: () => get<BusRouteDto[]>('/api/bus-routes'),
  feeStructures: () => get<FeeStructureDto[]>('/api/fee-structures'),
  scholarshipStructures: () => get<ScholarshipStructureDto[]>('/api/scholarship-structures'),
};

export const api = {
  baseUrl: API_BASE_URL,
};

// ---------------- Bulk Update API ----------------

export const bulkUpdateApi = {
  schema: () => get<BulkUpdateSchema>('/api/bulk-update/schema'),
  validate: (body: BulkUpdateRequest) =>
    post<BulkUpdatePreview>('/api/bulk-update/validate', body),
  apply: (body: BulkUpdateRequest) =>
    post<BulkUpdateApply>('/api/bulk-update/apply', body),
};

// ---------------- Bulk Add Admission API ----------------

export const bulkAdmissionApi = {
  schema: () => get<BulkUpdateSchema>('/api/bulk-admission/schema'),
  validate: (body: BulkUpdateRequest) =>
    post<BulkAdmissionPreview>('/api/bulk-admission/validate', body),
  apply: (body: BulkUpdateRequest) =>
    post<BulkAdmissionApply>('/api/bulk-admission/apply', body),
};

// ---------------- Admin Profile API ----------------

export const profileApi = {
  getProfile: () => get<AdminProfileDto>('/api/profile'),
  updateProfile: (body: UpdateAdminProfileRequest) =>
    put<AdminProfileDto>('/api/profile', body),
};
