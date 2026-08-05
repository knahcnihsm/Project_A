import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { StudentRecord } from '../types';
import { COLLEGE_INFO } from './constants';

export const generateStudentPdf = (student: StudentRecord) => {
  const doc = new jsPDF('p', 'mm', 'a4');

  // Header Banner
  doc.setFillColor(13, 71, 161);
  doc.rect(0, 0, 210, 30, 'F');

  doc.setTextColor(255, 255, 255);
  doc.setFontSize(14);
  doc.setFont('helvetica', 'bold');
  doc.text(COLLEGE_INFO.name, 105, 12, { align: 'center' });

  doc.setFontSize(10);
  doc.setFont('helvetica', 'normal');
  doc.text(`${COLLEGE_INFO.portalTitle} - Official Admission Summary`, 105, 19, { align: 'center' });
  doc.text(COLLEGE_INFO.tagline, 105, 25, { align: 'center' });

  let startY = 38;

  // Title
  doc.setTextColor(26, 43, 73);
  doc.setFontSize(13);
  doc.setFont('helvetica', 'bold');
  doc.text(`STUDENT ADMISSION PROFILE: ${student.personal.studentName.toUpperCase()}`, 14, startY);
  startY += 6;

  // Personal Info Table
  autoTable(doc, {
    startY,
    head: [['Personal Details', 'Information']],
    body: [
      ['Application Number', student.personal.applicationNumber],
      ['Register Number', student.personal.registerNumber],
      ['Student Name', student.personal.studentName],
      ['Date of Birth', `${student.personal.dateOfBirth} (Age: ${student.personal.age})`],
      ['Gender / Caste', `${student.personal.gender} / ${student.personal.caste}`],
      ['Aadhaar Number', student.personal.aadhaarNumber],
      ['District / Nationality', `${student.personal.district} / ${student.personal.nationality}`],
    ],
    theme: 'grid',
    headStyles: { fillColor: [21, 101, 192], textColor: [255, 255, 255] },
  });

  // Academic Details Table
  autoTable(doc, {
    startY: (doc as any).lastAutoTable.finalY + 6,
    head: [['Academic Details', 'Information']],
    body: [
      ['Admission Category', student.academic.admissionCategory],
      ['Program', student.academic.program],
      ['Department', student.academic.department],
      ['Batch', student.academic.batch],
      ['Date of Admission', student.academic.dateOfAdmission],
    ],
    theme: 'grid',
    headStyles: { fillColor: [21, 101, 192], textColor: [255, 255, 255] },
  });

  // Parent & Communication Table
  autoTable(doc, {
    startY: (doc as any).lastAutoTable.finalY + 6,
    head: [['Parent & Contact Details', 'Information']],
    body: [
      ['Father Name', student.parent.fatherName],
      ['Father Mobile', student.parent.fatherMobile],
      ['Annual Income', `₹ ${student.parent.annualIncome.toLocaleString('en-IN')}`],
      ['Mobile Number', student.communication.permanentAddress.mobileNumber],
      ['Email Address', student.communication.permanentAddress.email],
      ['Permanent Address', student.communication.permanentAddress.addressLine],
      ['PIN Code', student.communication.permanentAddress.pinCode],
    ],
    theme: 'grid',
    headStyles: { fillColor: [21, 101, 192], textColor: [255, 255, 255] },
  });

  // Fee Details Table
  autoTable(doc, {
    startY: (doc as any).lastAutoTable.finalY + 6,
    head: [['Fee Component', 'Amount (₹)']],
    body: [
      ['Tuition Fee (Per Year)', `₹ ${student.fee.tuitionFeePerYear.toLocaleString('en-IN')}`],
      [`Total Tuition Fee (${student.fee.courseDurationYears} Years)`, `₹ ${student.fee.totalTuitionFee.toLocaleString('en-IN')}`],
      ['Bus Fee', `₹ ${student.fee.busFee.toLocaleString('en-IN')}`],
      ['Hostel Fee', `₹ ${student.fee.hostelFee.toLocaleString('en-IN')}`],
      ['GRAND TOTAL FEE', `₹ ${student.fee.grandTotalFee.toLocaleString('en-IN')}`],
    ],
    theme: 'grid',
    headStyles: { fillColor: [13, 71, 161], textColor: [255, 255, 255] },
  });

  // Footer Signatures
  const pageHeight = doc.internal.pageSize.height;
  doc.setFontSize(9);
  doc.setTextColor(102, 112, 133);
  doc.text('Student Signature', 25, pageHeight - 15);
  doc.text('Parent Signature', 95, pageHeight - 15);
  doc.text('Principal / ERP Officer Signature', 150, pageHeight - 15);

  doc.save(`${student.personal.registerNumber}_Admission_Summary.pdf`);
};
