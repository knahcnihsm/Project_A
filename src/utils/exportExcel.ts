import * as XLSX from 'xlsx';
import { StudentRecord } from '../types';

export const exportStudentsToExcel = (students: StudentRecord[], filename: string = 'Students_List.xlsx') => {
  const exportData = students.map((s) => ({
    'Application No': s.personal.applicationNumber.toUpperCase(),
    'Register No': s.personal.registerNumber.toUpperCase(),
    'Student Name': s.personal.studentName.toUpperCase(),
    'DOB': s.personal.dateOfBirth,
    'Gender': s.personal.gender.toUpperCase(),
    'Aadhaar No': s.personal.aadhaarNumber.toUpperCase(),
    'District': s.personal.district.toUpperCase(),
    'Category': s.personal.caste.toUpperCase(),
    'Admission Category': s.academic.admissionCategory.toUpperCase(),
    'Program': s.academic.program.toUpperCase(),
    'Department': s.academic.department.toUpperCase(),
    'Batch': s.academic.batch.toUpperCase(),
    'Father Name': s.parent.fatherName.toUpperCase(),
    'Father Mobile': s.parent.fatherMobile.toUpperCase(),
    'Mobile Number': s.communication.permanentAddress.mobileNumber.toUpperCase(),
    'Email': s.communication.permanentAddress.email.toUpperCase(),
    'Cut-Off Mark': s.fee.cutOffMark ?? 'N/A',
    'Merit Score (%)': s.fee.meritPercent ?? 'N/A',
    'Original Tuition Fee (₹/Yr)': s.fee.originalTuitionFee ?? 'N/A',
    'Scholarship Amount (₹/Yr)': s.fee.scholarshipAmount ?? 'N/A',
    'Final Tuition Fee (₹/Yr)': s.fee.tuitionFeePerYear,
    'Total Tuition Fee (₹)': s.fee.totalTuitionFee,
    'Grand Total Fee (₹)': s.fee.grandTotalFee,
    'Status': s.status.toUpperCase(),
    'Archive Reason': (s.archiveReason || 'N/A').toUpperCase(),
  }));

  const worksheet = XLSX.utils.json_to_sheet(exportData);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Students');
  XLSX.writeFile(workbook, filename);
};
