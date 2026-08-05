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
  const sampleData = [
    {
      'Application Number': 'RGCET/2026/001',
      'Register Number': '2026BTECH001',
      'Student Name': 'Aravind Kumar',
      'Father Name': 'R. Kumar',
      'Father Mobile': '9876543210',
      'District': 'Puducherry',
      'Mobile Number': '9123456789',
      'Email': 'aravind@example.com',
    },
    {
      'Application Number': 'RGCET/2026/002',
      'Register Number': '2026BTECH002',
      'Student Name': 'Priya Dharshini',
      'Father Name': 'S. Murugan',
      'Father Mobile': '9845123760',
      'District': 'Cuddalore',
      'Mobile Number': '9876541230',
      'Email': 'priya@example.com',
    },
  ];

  const worksheet = XLSX.utils.json_to_sheet(sampleData);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Bulk_Update_Template');
  XLSX.writeFile(workbook, 'Bulk_Student_Update_Template.xlsx');
};
