import * as XLSX from 'xlsx';
import {
  BulkWorkbookRequest,
  BulkIssueDto,
  BulkPreviewRowDto,
  BulkCommitResponse,
  BulkSubjectRowRequest,
} from '../api/types';

export const BULK_SHEET_NAMES: string[] = [
  'Student Personal Details',
  'Parent-Guardian Details',
  'Communication Address',
  'Permanent Address',
  'Academic Admission Details',
  'Qualifying Exam (HSC-CBSE)',
  'HSC Marks',
  'Diploma Qualification Details',
  'PG Qualifying Degree Details',
  'Instructions',
];

const DATA_SHEETS = BULK_SHEET_NAMES.slice(0, 9);

const PERSONAL_COLUMNS = [
  'Application No',
  'Register No',
  'Student Name',
  'Date of Birth',
  'Gender',
  'Aadhaar No',
  'Nationality',
  'District',
  'Caste / Category',
];

const PARENT_COLUMNS = [
  'Application No',
  'Register No',
  'Father Name',
  'Father Mobile',
  'Father Occupation',
  'Annual Family Income',
];

const ADDRESS_COLUMNS = [
  'Application No',
  'Register No',
  'Address Line',
  'PIN Code',
  'Landline No',
  'Mobile No',
  'Email ID',
];

const ACADEMIC_COLUMNS = [
  'Application No',
  'Register No',
  'Admission Category',
  'Program',
  'Department',
  'Batch',
  'Date of Admission',
];

const QUALIFYING_COLUMNS = [
  'Application No',
  'Register No',
  'Institution Name',
  'Institution Place',
  'Exam Passed',
  'Month & Year of Passing',
  'SSLC Percentage',
  'SSLC Register Number',
  'HSC Percentage',
  'HSC Register Number',
];

const HSC_COLUMNS = [
  'Application No',
  'Register No',
  'Stream',
  'Section',
  'Subject Name',
  'Month & Year',
  'Maximum Marks',
  'Marks Obtained',
];

const DIPLOMA_COLUMNS = [
  'Application No',
  'Register No',
  'Diploma Course',
  'Institution Name',
  'Board',
  'Second Year Percentage',
  'Third Year Percentage',
  'Aggregate Percentage',
];

const PG_COLUMNS = [
  'Application No',
  'Register No',
  'University Name',
  'University Place',
  'Institution Name',
  'Institution Place',
  'Exam Passed',
  'Month & Year of Passing',
  'Total Percentage',
  'Main Subject Percentage',
  'Degree Registration Number',
];

export interface BulkSheetStat {
  name: string;
  dataRows: number;
  found: boolean;
}

export interface BulkWorkbookParseResult {
  fileName: string;
  workbook: BulkWorkbookRequest;
  sheetsFound: string[];
  sheetsMissing: string[];
  sheetStats: BulkSheetStat[];
  totalRows: number;
}

const dateToIso = (d: Date): string => {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

const toCellText = (v: unknown): string | undefined => {
  if (v === undefined || v === null) return undefined;
  if (v instanceof Date) {
    if (Number.isNaN(v.getTime())) return undefined;
    return dateToIso(v);
  }
  if (typeof v === 'number') {
    if (!Number.isFinite(v)) return undefined;
    return String(v);
  }
  const s = String(v).trim();
  return s || undefined;
};

const cellValue = (row: Record<string, unknown>, keys: string[]): string | undefined => {
  for (const key of keys) {
    const v = row[key];
    if (v === undefined || v === null) continue;
    const s = toCellText(v);
    if (s) return s;
  }
  return undefined;
};

// ---------------- Template builder ----------------

const appendDataSheet = (
  wb: XLSX.WorkBook,
  name: string,
  headers: string[],
  sampleRows: unknown[][]
) => {
  const aoa: unknown[][] = [headers];
  sampleRows.forEach((r) => aoa.push(r));
  const ws = XLSX.utils.aoa_to_sheet(aoa);
  ws['!cols'] = headers.map((h) => ({ wch: Math.max(12, h.length + 2) }));
  ws['!freeze'] = { xSplit: 0, ySplit: 1 };
  XLSX.utils.book_append_sheet(wb, ws, name);
};

const buildInstructions = (sample: boolean): string[] => {
  const lines: string[] = [
    'BULK STUDENT DETAILS UPDATE - INSTRUCTIONS',
    '',
    '1. PURPOSE',
    '   Fill in the Excel sheets below to update existing student records in bulk.',
    '   All sheets are committed together in a single transaction: if one row fails',
    '   validation, NO rows are updated.',
    '',
    '2. IDENTIFIERS',
    '   Every data row must contain the student\'s Application No or Register No',
    '   exactly as stored in the ERP. Students are matched against these values.',
    '   Rows that match no existing student are reported as errors and block the commit.',
    '',
    '3. BLANK CELLS = NO CHANGE',
    '   Leave a cell empty to keep the existing value. Only filled-in cells are applied.',
    '   Do NOT delete or rename the header row or the sheet names.',
    '',
    '4. FILL ONLY WHAT YOU NEED',
    '   You can update a single student, or thousands, in one workbook. Unused sheets',
    '   may be left completely empty (only the header row).',
    '',
    '5. VALIDATION RULES',
    '   ERROR (blocks the entire commit):',
    '   - Gender: Male / Female / Transgender (or M / F / T)',
    '   - Caste / Category: OC, BC, BCM, MBC, SC, SCA, ST (OBC is accepted as BC)',
    '   - Aadhaar No: exactly 12 digits',
    '   - Father Mobile / Mobile No: exactly 10 digits',
    '   - Email ID: must be a valid email address',
    '   - PIN Code: exactly 6 digits',
    '   - Percentages: between 0 and 100',
    '   - Marks: not negative and not more than the Maximum Marks',
    '   - Admission Category / Program / Department must match master data',
    '',
    '   WARNING (never blocks the commit):',
    '   - Nationality, District, Batch, Board, institution names and Month & Year',
    '     are free text - unusual values are accepted with a warning.',
    '',
    '6. DERIVED VALUES',
    '   Age, overall percentage, engineering cut-off, diploma aggregate and fees are',
    '   recalculated automatically by the system - do not fill them.',
    '',
    '7. SHEET 7 - HSC MARKS',
    '   Enter ONE row per subject. Section must be "Academic" or "Vocational".',
    '   Academic stream typically has 4 subjects (Mathematics, Physics, Chemistry',
    '   and one more science / Computer Science / Biology).',
    '   Vocational stream typically has 3 subjects.',
    '   Month & Year is optional for each subject row.',
    '',
    '8. SHEETS OVERVIEW',
    '   1  Student Personal Details',
    '   2  Parent / Guardian Details',
    '   3  Communication Address',
    '   4  Permanent Address',
    '   5  Academic Admission Details',
    '   6  Qualifying Examination (HSC / CBSE)',
    '   7  HSC Marks',
    '   8  Diploma Qualification Details',
    '   9  PG Qualifying Degree Details',
    '',
    '9. DATE FORMAT',
    '   Use YYYY-MM-DD (e.g. 2007-05-14) or any common format like DD-MM-YYYY.',
    '',
  ];
  if (sample) {
    lines.push(
      '',
      '10. SAMPLE DATA',
      '   The other sheets contain one example row for a fictional student. Replace',
      '   the values with real student data before uploading. Rows that do not match',
      '   an existing student are rejected by the system.',
      ''
    );
  }
  return lines;
};

/**
 * Builds the official 10-sheet Bulk Student Update workbook.
 * @param sample when true, data sheets are pre-filled with one example row.
 */
export const buildBulkWorkbook = (sample: boolean): XLSX.WorkBook => {
  const wb = XLSX.utils.book_new();

  const personalSample = sample
    ? [['RGCET/2026/9990', '26BTECH990', 'Arun Kumar', '2007-05-14', 'Male', '123456789012', 'Indian', 'Coimbatore', 'BC']]
    : [];
  appendDataSheet(wb, 'Student Personal Details', PERSONAL_COLUMNS, personalSample);

  const parentSample = sample
    ? [['RGCET/2026/9990', '26BTECH990', 'Kumarasamy', '9843012345', 'Farmer', '250000']]
    : [];
  appendDataSheet(wb, 'Parent-Guardian Details', PARENT_COLUMNS, parentSample);

  const commSample = sample
    ? [['RGCET/2026/9990', '26BTECH990', '12, Gandhi Street, Coimbatore', '641001', '04224231234', '9843012345', 'arun.kumar@example.com']]
    : [];
  appendDataSheet(wb, 'Communication Address', ADDRESS_COLUMNS, commSample);

  const permSample = sample
    ? [['RGCET/2026/9990', '26BTECH990', '12, Gandhi Street, Coimbatore', '641001', '04224231234', '9843012345', 'arun.kumar@example.com']]
    : [];
  appendDataSheet(wb, 'Permanent Address', ADDRESS_COLUMNS, permSample);

  const academicSample = sample
    ? [['RGCET/2026/9990', '26BTECH990', 'Management', 'First Year B.Tech', 'Computer Science and Engineering', '2026-2030', '2026-08-01']]
    : [];
  appendDataSheet(wb, 'Academic Admission Details', ACADEMIC_COLUMNS, academicSample);

  const qualifyingSample = sample
    ? [['RGCET/2026/9990', '26BTECH990', 'Government Higher Secondary School', 'Coimbatore', 'HSC', 'May 2025', '85.5', 'SSLC2020123', '83.29', 'HSC2020456']]
    : [];
  appendDataSheet(wb, 'Qualifying Exam (HSC-CBSE)', QUALIFYING_COLUMNS, qualifyingSample);

  const hscSample = sample
    ? [
        ['RGCET/2026/9990', '26BTECH990', 'Science', 'Academic', 'Mathematics', 'May 2025', '100', '92'],
        ['RGCET/2026/9990', '26BTECH990', 'Science', 'Academic', 'Physics', 'May 2025', '100', '78'],
        ['RGCET/2026/9990', '26BTECH990', 'Science', 'Academic', 'Chemistry', 'May 2025', '100', '81'],
        ['RGCET/2026/9990', '26BTECH990', 'Science', 'Academic', 'Computer Science', 'May 2025', '100', '85'],
        ['RGCET/2026/9990', '26BTECH990', 'Science', 'Vocational', 'Vocational Subject', 'May 2025', '100', '74'],
        ['RGCET/2026/9990', '26BTECH990', 'Science', 'Vocational', 'Related Subject I', 'May 2025', '100', '70'],
        ['RGCET/2026/9990', '26BTECH990', 'Science', 'Vocational', 'Related Subject II', 'May 2025', '100', '68'],
      ]
    : [];
  appendDataSheet(wb, 'HSC Marks', HSC_COLUMNS, hscSample);

  const diplomaSample = sample
    ? [['RGCET/2026/9991', '26BTECH991', 'Diploma in Mechanical Engineering', 'PSG Polytechnic', 'DOTE', '80', '82', '81.5']]
    : [];
  appendDataSheet(wb, 'Diploma Qualification Details', DIPLOMA_COLUMNS, diplomaSample);

  const pgSample = sample
    ? [['RGCET/2026/9992', '26PG9992', 'Anna University', 'Chennai', 'RGCET', 'Coimbatore', 'B.E.', 'May 2024', '75', '78', 'PGREG001']]
    : [];
  appendDataSheet(wb, 'PG Qualifying Degree Details', PG_COLUMNS, pgSample);

  const ws = XLSX.utils.aoa_to_sheet(buildInstructions(sample).map((line) => [line]));
  ws['!cols'] = [{ wch: 115 }];
  XLSX.utils.book_append_sheet(wb, ws, 'Instructions');

  return wb;
};

export const downloadBulkTemplate = (sample: boolean): void => {
  const wb = buildBulkWorkbook(sample);
  XLSX.writeFile(wb, sample ? 'Bulk_Student_Update_Sample.xlsx' : 'Bulk_Student_Update_Template.xlsx');
};

// ---------------- Workbook parser ----------------

const sheetToRecords = (ws: XLSX.WorkSheet): Record<string, unknown>[] =>
  XLSX.utils.sheet_to_json<Record<string, unknown>>(ws, { defval: '' });

const buildRequest = (fileName: string): BulkWorkbookRequest => ({
  fileName,
  uploadedBy: 'Admin',
  personal: [],
  parent: [],
  communication: [],
  permanent: [],
  academic: [],
  qualifyingExam: [],
  hscMarks: [],
  diploma: [],
  pg: [],
});

export const parseBulkWorkbookFile = async (file: File): Promise<BulkWorkbookParseResult> => {
  const buffer = await file.arrayBuffer();
  const wb = XLSX.read(buffer, { type: 'array', cellDates: true });
  return parseBulkWorkbook(wb, file.name);
};

export const parseBulkWorkbook = (wb: XLSX.WorkBook, fileName: string): BulkWorkbookParseResult => {
  const request = buildRequest(fileName);

  const sheetsFound: string[] = [];
  const sheetsMissing: string[] = [];
  const sheetStats: BulkSheetStat[] = [];
  let totalRows = 0;

  const parseSheet = (
    sheetName: string,
    mapper: (row: Record<string, unknown>, rowNumber: number) => void
  ) => {
    const stat: BulkSheetStat = { name: sheetName, dataRows: 0, found: false };
    const ws = wb.Sheets[sheetName];
    if (ws) {
      stat.found = true;
      sheetsFound.push(sheetName);
      const records = sheetToRecords(ws);
      records.forEach((row, index) => {
        const rowNumber = index + 2;
        mapper(row, rowNumber);
      });
      stat.dataRows = records.length;
      totalRows += records.length;
    } else {
      sheetsMissing.push(sheetName);
    }
    sheetStats.push(stat);
  };

  parseSheet('Student Personal Details', (row, rowNumber) => {
    request.personal.push({
      rowNumber,
      applicationNo: cellValue(row, ['Application No', 'Application Number', 'ApplicationNo']),
      registerNo: cellValue(row, ['Register No', 'Register Number', 'RegisterNo']),
      studentName: cellValue(row, ['Student Name', 'StudentName']),
      dateOfBirth: cellValue(row, ['Date of Birth', 'DOB']),
      gender: cellValue(row, ['Gender']),
      aadhaarNumber: cellValue(row, ['Aadhaar No', 'Aadhaar Number', 'Aadhaar']),
      nationality: cellValue(row, ['Nationality']),
      district: cellValue(row, ['District']),
      caste: cellValue(row, ['Caste / Category', 'Caste', 'Category']),
    });
  });

  parseSheet('Parent-Guardian Details', (row, rowNumber) => {
    request.parent.push({
      rowNumber,
      applicationNo: cellValue(row, ['Application No', 'Application Number', 'ApplicationNo']),
      registerNo: cellValue(row, ['Register No', 'Register Number', 'RegisterNo']),
      fatherName: cellValue(row, ['Father Name']),
      fatherMobile: cellValue(row, ['Father Mobile', 'Father Mobile Number']),
      fatherOccupation: cellValue(row, ['Father Occupation']),
      annualIncome: cellValue(row, ['Annual Family Income', 'Annual Income']),
    });
  });

  const mapAddress = (row: Record<string, unknown>, rowNumber: number, permanent: boolean) => {
    const landlineKeys = permanent
      ? ['Permanent Landline No', 'Landline No', 'Phone', 'Landline']
      : ['Communication Landline No', 'Landline No', 'Phone', 'Landline'];
    const address = {
      rowNumber,
      applicationNo: cellValue(row, ['Application No', 'Application Number', 'ApplicationNo']),
      registerNo: cellValue(row, ['Register No', 'Register Number', 'RegisterNo']),
      addressLine: cellValue(row, ['Address Line', 'Address']),
      pincode: cellValue(row, ['PIN Code', 'Pincode', 'PIN']),
      phone: cellValue(row, landlineKeys),
      mobile: cellValue(row, ['Mobile No', 'Mobile Number', 'Mobile']),
      email: cellValue(row, ['Email ID', 'Email']),
    };
    return address;
  };

  parseSheet('Communication Address', (row, rowNumber) => {
    request.communication.push(mapAddress(row, rowNumber, false));
  });

  parseSheet('Permanent Address', (row, rowNumber) => {
    request.permanent.push(mapAddress(row, rowNumber, true));
  });

  parseSheet('Academic Admission Details', (row, rowNumber) => {
    request.academic.push({
      rowNumber,
      applicationNo: cellValue(row, ['Application No', 'Application Number', 'ApplicationNo']),
      registerNo: cellValue(row, ['Register No', 'Register Number', 'RegisterNo']),
      admissionCategory: cellValue(row, ['Admission Category']),
      program: cellValue(row, ['Program']),
      department: cellValue(row, ['Department']),
      batch: cellValue(row, ['Batch']),
      dateOfAdmission: cellValue(row, ['Date of Admission']),
    });
  });

  parseSheet('Qualifying Exam (HSC-CBSE)', (row, rowNumber) => {
    request.qualifyingExam.push({
      rowNumber,
      applicationNo: cellValue(row, ['Application No', 'Application Number', 'ApplicationNo']),
      registerNo: cellValue(row, ['Register No', 'Register Number', 'RegisterNo']),
      institutionName: cellValue(row, ['Institution Name']),
      institutionPlace: cellValue(row, ['Institution Place']),
      examPassed: cellValue(row, ['Exam Passed']),
      monthYearPassing: cellValue(row, ['Month & Year of Passing', 'Month and Year of Passing']),
      sslcPercentage: cellValue(row, ['SSLC Percentage']),
      sslcRegisterNumber: cellValue(row, ['SSLC Register Number', 'SSLC Register No']),
      hscPercentage: cellValue(row, ['HSC Percentage']),
      hscRegisterNumber: cellValue(row, ['HSC Register Number', 'HSC Register No']),
    });
  });

  const hscGroups = new Map<
    string,
    {
      rowNumber: number;
      applicationNo?: string;
      registerNo?: string;
      stream?: string;
      academic: BulkSubjectRowRequest[];
      vocational: BulkSubjectRowRequest[];
    }
  >();

  parseSheet('HSC Marks', (row, rowNumber) => {
    const applicationNo = cellValue(row, ['Application No', 'Application Number', 'ApplicationNo']);
    const registerNo = cellValue(row, ['Register No', 'Register Number', 'RegisterNo']);
    const stream = cellValue(row, ['Stream']);
    const section = (cellValue(row, ['Section']) || '').toLowerCase();
    const subject = cellValue(row, ['Subject Name', 'Subject']);
    const monthYear = cellValue(row, ['Month & Year', 'Month Year']);
    const maxMarks = cellValue(row, ['Maximum Marks', 'Max Marks']);
    const marksObtained = cellValue(row, ['Marks Obtained', 'Obtained']);

    const key = registerNo || applicationNo || `__row${rowNumber}__`;
    let group = hscGroups.get(key);
    if (!group) {
      group = {
        rowNumber,
        applicationNo,
        registerNo,
        stream,
        academic: [],
        vocational: [],
      };
      hscGroups.set(key, group);
    } else if (!group.stream && stream) {
      group.stream = stream;
    }

    const subjectRow: BulkSubjectRowRequest = { subject, monthYear, maxMarks, marksObtained };
    if (section.startsWith('v')) {
      group.vocational.push(subjectRow);
    } else {
      group.academic.push(subjectRow);
    }
  });

  hscGroups.forEach((group) => {
    request.hscMarks.push({
      rowNumber: group.rowNumber,
      applicationNo: group.applicationNo,
      registerNo: group.registerNo,
      stream: group.stream,
      academicMarks: group.academic,
      vocationalMarks: group.vocational,
    });
  });

  parseSheet('Diploma Qualification Details', (row, rowNumber) => {
    request.diploma.push({
      rowNumber,
      applicationNo: cellValue(row, ['Application No', 'Application Number', 'ApplicationNo']),
      registerNo: cellValue(row, ['Register No', 'Register Number', 'RegisterNo']),
      diplomaCourse: cellValue(row, ['Diploma Course', 'Diploma']),
      institutionName: cellValue(row, ['Institution Name']),
      board: cellValue(row, ['Board']),
      secondYearPercentage: cellValue(row, ['Second Year Percentage']),
      thirdYearPercentage: cellValue(row, ['Third Year Percentage']),
      aggregatePercentage: cellValue(row, ['Aggregate Percentage']),
    });
  });

  parseSheet('PG Qualifying Degree Details', (row, rowNumber) => {
    request.pg.push({
      rowNumber,
      applicationNo: cellValue(row, ['Application No', 'Application Number', 'ApplicationNo']),
      registerNo: cellValue(row, ['Register No', 'Register Number', 'RegisterNo']),
      universityName: cellValue(row, ['University Name']),
      universityPlace: cellValue(row, ['University Place']),
      institutionName: cellValue(row, ['Institution Name']),
      institutionPlace: cellValue(row, ['Institution Place']),
      examPassed: cellValue(row, ['Exam Passed']),
      monthYearPassing: cellValue(row, ['Month & Year of Passing', 'Month and Year of Passing']),
      totalPercentage: cellValue(row, ['Total Percentage']),
      mainSubjectPercentage: cellValue(row, ['Main Subject Percentage']),
      degreeRegistrationNumber: cellValue(row, ['Degree Registration Number', 'Degree Reg No']),
    });
  });

  return {
    fileName,
    workbook: request,
    sheetsFound,
    sheetsMissing,
    sheetStats,
    totalRows,
  };
};

// ---------------- Report exporters ----------------

export const exportBulkIssuesReport = (
  issues: BulkIssueDto[],
  filename: string = 'Bulk_Update_Issues.xlsx'
): void => {
  const rows = issues.map((i) => ({
    'Sheet': i.sheet,
    'Row Number': i.rowNumber,
    'Field': i.field,
    'Severity': i.severity,
    'Message': i.message,
  }));
  const ws = XLSX.utils.json_to_sheet(rows);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Issues');
  XLSX.writeFile(wb, filename);
};

export const exportBulkPreviewReport = (
  preview: BulkPreviewRowDto[],
  filename: string = 'Bulk_Update_Preview.xlsx'
): void => {
  const rows: Record<string, string>[] = [];
  preview.forEach((p) => {
    const base = {
      'Student Name': p.studentName ?? '',
      'Application No': p.applicationNo ?? '',
      'Register No': p.registerNo ?? '',
    };
    const changes = p.changes.length > 0 ? p.changes : [{ sheet: '', field: '', oldValue: '', newValue: '' }];
    changes.forEach((c) => {
      rows.push({
        ...base,
        'Sheet': c.sheet,
        'Field': c.field,
        'Old Value': c.oldValue ?? '',
        'New Value': c.newValue ?? '',
      });
    });
  });
  const ws = XLSX.utils.json_to_sheet(rows);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Preview');
  XLSX.writeFile(wb, filename);
};

export const exportBulkCommitReport = (
  response: BulkCommitResponse,
  filename: string = 'Bulk_Update_Report.xlsx'
): void => {
  const wb = XLSX.utils.book_new();
  const summaryWs = XLSX.utils.aoa_to_sheet([
    ['Bulk Student Update Report'],
    [''],
    ['File Name', response.fileName ?? ''],
    ['Uploaded By', response.uploadedBy ?? ''],
    ['Uploaded At', response.uploadedAt ?? ''],
    ['Status', response.status],
    ['Total Rows', response.totalRows],
    ['Valid Rows', response.validRows],
    ['Students Updated', response.updatedStudents],
    ['Rows With No Change', response.noChangeRows],
    ['Skipped Rows', response.skippedRows],
    ['Failed Rows', response.failedRows],
    ['Warnings', response.warningCount],
    ['Duration (ms)', response.durationMs],
  ]);
  summaryWs['!cols'] = [{ wch: 28 }, { wch: 24 }];
  XLSX.utils.book_append_sheet(wb, summaryWs, 'Summary');

  if (response.issues.length > 0) {
    const issuesWs = XLSX.utils.json_to_sheet(
      response.issues.map((i) => ({
        'Sheet': i.sheet,
        'Row Number': i.rowNumber,
        'Field': i.field,
        'Severity': i.severity,
        'Message': i.message,
      }))
    );
    XLSX.utils.book_append_sheet(wb, issuesWs, 'Issues');
  }
  XLSX.writeFile(wb, filename);
};

export { DATA_SHEETS };
