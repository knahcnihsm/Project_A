import * as XLSX from 'xlsx';
import { StudentRecord } from '../types';

export const exportStudentsToExcel = (students: StudentRecord[], filename: string = 'Students_List.xlsx') => {
  const exportData = students.map((s) => ({
    'Application No': s.personal.applicationNumber,
    'Register No': s.personal.registerNumber,
    'Student Name': s.personal.studentName,
    'DOB': s.personal.dateOfBirth,
    'Gender': s.personal.gender,
    'Aadhaar No': s.personal.aadhaarNumber,
    'District': s.personal.district,
    'Category': s.personal.caste,
    'Admission Category': s.academic.admissionCategory,
    'Program': s.academic.program,
    'Department': s.academic.department,
    'Batch': s.academic.batch,
    'Father Name': s.parent.fatherName,
    'Father Mobile': s.parent.fatherMobile,
    'Mobile Number': s.communication.permanentAddress.mobileNumber,
    'Email': s.communication.permanentAddress.email,
    'Grand Total Fee (₹)': s.fee.grandTotalFee,
    'Status': s.status,
    'Archive Reason': s.archiveReason || 'N/A',
  }));

  const worksheet = XLSX.utils.json_to_sheet(exportData);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Students');
  XLSX.writeFile(workbook, filename);
};

export const downloadSampleBulkUpdateTemplate = () => {
  const link = document.createElement('a');
  link.href = '/sample_template.xlsx';
  link.download = 'sample_template.xlsx';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

export interface BulkUpdateErrorRow {
  rowNumber: number;
  registerNumber: string;
  applicationNumber: string;
  reason: string;
}

export const exportBulkUpdateErrorReport = (
  errors: BulkUpdateErrorRow[],
  filename: string = 'Bulk_Update_Error_Report.xlsx'
) => {
  const exportData = errors.map((e) => ({
    'Row Number': e.rowNumber,
    'Register Number': e.registerNumber || 'N/A',
    'Application Number': e.applicationNumber || 'N/A',
    'Error Reason': e.reason,
  }));

  const worksheet = XLSX.utils.json_to_sheet(exportData);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Error Report');
  XLSX.writeFile(workbook, filename);
};
