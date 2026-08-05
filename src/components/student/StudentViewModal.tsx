import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Box,
  Typography,
  Grid,
  Button,
  IconButton,
  Chip,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Avatar,
  Divider,
  Paper,
  Tooltip,
} from '@mui/material';
import {
  X,
  Printer,
  Download,
  Eye,
  CheckCircle,
  XCircle,
  User,
  Heart,
  Users,
  MapPin,
  GraduationCap,
  BookOpen,
  CreditCard,
  Bus,
  Home,
  FileCheck,
  Calendar,
  Phone,
  Mail,
  Shield,
  Award,
  Hash,
} from 'lucide-react';
import { useAdmission } from '../../context/AdmissionContext';
import { useThemeContext } from '../../context/ThemeContext';
import { formatDateDisplay } from '../../utils/dateUtils';
import { generateStudentPdf } from '../../utils/exportPdf';
import { CertificateItem, StudentRecord } from '../../types';

// Helper component for styled read-only field boxes
const DetailField: React.FC<{
  label: string;
  value: React.ReactNode;
  icon?: React.ReactNode;
  fullWidth?: boolean;
}> = ({ label, value, icon, fullWidth = false }) => {
  const { mode } = useThemeContext();
  const isDark = mode === 'dark';

  return (
    <Box
      sx={{
        width: '100%',
        backgroundColor: isDark ? '#0F172A' : '#F8FAFC',
        border: `1px solid ${isDark ? '#334155' : '#E2E8F0'}`,
        borderRadius: '12px',
        padding: '12px 16px',
        display: 'flex',
        flexDirection: 'column',
        gap: '4px',
        transition: 'all 150ms ease-in-out',
        '&:hover': {
          borderColor: isDark ? '#475569' : '#CBD5E1',
          backgroundColor: isDark ? '#1E293B' : '#F1F5F9',
        },
      }}
    >
      <Typography
        variant="caption"
        sx={{
          fontWeight: 600,
          fontSize: '11.5px',
          color: isDark ? '#94A3B8' : '#64748B',
          letterSpacing: '0.04em',
          textTransform: 'uppercase',
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
        }}
      >
        {icon}
        {label}
      </Typography>
      <Typography
        variant="body1"
        sx={{
          fontWeight: 600,
          fontSize: '14.5px',
          color: isDark ? '#F8FAFC' : '#1E293B',
          wordBreak: 'break-word',
        }}
      >
        {value !== undefined && value !== null && value !== '' ? (
          value
        ) : (
          <Typography component="span" sx={{ color: isDark ? '#64748B' : '#94A3B8', fontStyle: 'italic', fontSize: '13.5px' }}>
            N/A
          </Typography>
        )}
      </Typography>
    </Box>
  );
};

// Section Header Component
const SectionHeader: React.FC<{ title: string; icon: React.ReactNode }> = ({ title, icon }) => {
  const { mode } = useThemeContext();
  const isDark = mode === 'dark';

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        marginBottom: '16px',
        paddingBottom: '8px',
        borderBottom: `2px solid ${isDark ? '#334155' : '#E2EBF6'}`,
      }}
    >
      <Box
        sx={{
          width: '36px',
          height: '36px',
          borderRadius: '10px',
          backgroundColor: isDark ? 'rgba(56, 189, 248, 0.15)' : '#E0F2FE',
          color: isDark ? '#38BDF8' : '#0B3D91',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {icon}
      </Box>
      <Typography
        variant="h6"
        sx={{
          fontWeight: 700,
          fontSize: '16px',
          letterSpacing: '0.05em',
          color: isDark ? '#FFFFFF' : '#0B3D91',
          textTransform: 'uppercase',
        }}
      >
        {title}
      </Typography>
    </Box>
  );
};

export const StudentViewModal: React.FC = () => {
  const { mode } = useThemeContext();
  const isDark = mode === 'dark';
  const { viewModalStudent, closeViewModal, showSnackbar } = useAdmission();

  const [previewCert, setPreviewCert] = useState<CertificateItem | null>(null);

  if (!viewModalStudent) return null;

  const s = viewModalStudent;

  const handlePrint = () => {
    window.print();
  };

  const handleDownloadCert = (cert: CertificateItem) => {
    showSnackbar(`Downloading ${cert.name} (${cert.fileName || cert.name + '.pdf'})...`);
  };

  // Helper values & defaults for comprehensive display
  const studentName = s.personal.studentName || 'STUDENT';
  const regNo = s.personal.registerNumber || 'N/A';
  const appNo = s.personal.applicationNumber || 'N/A';
  const dept = s.academic.department || 'N/A';
  const admissionType = s.academic.admissionCategory || 'N/A';
  const isArchived = s.status === 'Archived';

  // Fee calculation values
  const tuitionFee = s.fee.tuitionFeePerYear || 0;
  const busFee = s.fee.busFee || 0;
  const hostelFee = s.fee.hostelFee || 0;
  const totalFee = s.fee.grandTotalFee || tuitionFee * (s.fee.courseDurationYears || 4) + busFee + hostelFee;
  const paidAmount = s.fee.paidAmount ?? Math.round(totalFee * 0.45);
  const pendingAmount = s.fee.pendingAmount ?? totalFee - paidAmount;
  const paymentStatus = s.fee.paymentStatus || (pendingAmount === 0 ? 'Paid' : paidAmount > 0 ? 'Partial' : 'Pending');

  // Sample Receipt History if not present
  const receiptHistory = s.fee.receiptHistory || [
    {
      receiptNo: `REC-2026-0${s.id.slice(-2)}1`,
      date: s.academic.dateOfAdmission || '2026-07-29',
      amount: Math.round(paidAmount * 0.6),
      mode: 'Online' as const,
      status: 'Completed' as const,
    },
    {
      receiptNo: `REC-2026-0${s.id.slice(-2)}2`,
      date: '2026-08-01',
      amount: Math.round(paidAmount * 0.4),
      mode: 'UPI' as const,
      status: 'Completed' as const,
    },
  ];

  return (
    <>
      <Dialog
        open={Boolean(viewModalStudent)}
        onClose={closeViewModal}
        maxWidth={false}
        scroll="paper"
        PaperProps={{
          sx: {
            width: '90vw',
            maxWidth: '1400px',
            height: '90vh',
            maxHeight: '90vh',
            borderRadius: '18px',
            backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
            color: isDark ? '#FFFFFF' : '#1E293B',
            boxShadow: isDark
              ? '0 25px 50px -12px rgba(0, 0, 0, 0.7)'
              : '0 25px 50px -12px rgba(11, 61, 145, 0.15)',
            border: `1px solid ${isDark ? '#334155' : '#D6E4F0'}`,
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
          },
        }}
      >
        {/* ==================== MODAL HEADER ==================== */}
        <DialogTitle
          sx={{
            padding: '20px 28px',
            backgroundColor: isDark ? '#0F172A' : '#0B3D91',
            color: '#FFFFFF',
            borderBottom: `1px solid ${isDark ? '#334155' : '#1E5EFF'}`,
          }}
        >
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
            {/* Title & Badge */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
              <Avatar
                sx={{
                  width: 48,
                  height: 48,
                  backgroundColor: '#38BDF8',
                  color: '#0B3D91',
                  fontWeight: 800,
                  fontSize: '20px',
                  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)',
                }}
              >
                {studentName.charAt(0)}
              </Avatar>
              <Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <Typography variant="h5" sx={{ fontWeight: 800, fontSize: '20px', color: '#FFFFFF', letterSpacing: '0.02em' }}>
                    {studentName}
                  </Typography>
                  <Chip
                    label={isArchived ? 'ARCHIVED' : 'ACTIVE'}
                    color={isArchived ? 'error' : 'success'}
                    size="small"
                    sx={{ fontWeight: 700, fontSize: '11px', height: '22px' }}
                  />
                </Box>
                <Typography variant="body2" sx={{ color: '#93C5FD', fontSize: '13px', fontWeight: 500, mt: '2px' }}>
                  Student Profile | Admission Portal
                </Typography>
              </Box>
            </Box>

            {/* Top Quick Meta Badges */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
              <Box sx={{ backgroundColor: 'rgba(255, 255, 255, 0.1)', padding: '6px 12px', borderRadius: '8px', textAlign: 'center' }}>
                <Typography variant="caption" sx={{ color: '#93C5FD', display: 'block', fontSize: '10px', textTransform: 'uppercase' }}>
                  Reg Number
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 700, color: '#FFFFFF', fontSize: '13px' }}>
                  {regNo}
                </Typography>
              </Box>

              <Box sx={{ backgroundColor: 'rgba(255, 255, 255, 0.1)', padding: '6px 12px', borderRadius: '8px', textAlign: 'center' }}>
                <Typography variant="caption" sx={{ color: '#93C5FD', display: 'block', fontSize: '10px', textTransform: 'uppercase' }}>
                  App Number
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 700, color: '#FFFFFF', fontSize: '13px' }}>
                  {appNo}
                </Typography>
              </Box>

              <Box sx={{ backgroundColor: 'rgba(255, 255, 255, 0.1)', padding: '6px 12px', borderRadius: '8px', textAlign: 'center' }}>
                <Typography variant="caption" sx={{ color: '#93C5FD', display: 'block', fontSize: '10px', textTransform: 'uppercase' }}>
                  Department
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 700, color: '#FFFFFF', fontSize: '13px' }}>
                  {dept}
                </Typography>
              </Box>

              <Box sx={{ backgroundColor: 'rgba(255, 255, 255, 0.1)', padding: '6px 12px', borderRadius: '8px', textAlign: 'center' }}>
                <Typography variant="caption" sx={{ color: '#93C5FD', display: 'block', fontSize: '10px', textTransform: 'uppercase' }}>
                  Admission Type
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 700, color: '#FFFFFF', fontSize: '13px' }}>
                  {admissionType}
                </Typography>
              </Box>

              <IconButton
                onClick={closeViewModal}
                sx={{
                  color: '#FFFFFF',
                  backgroundColor: 'rgba(255, 255, 255, 0.15)',
                  '&:hover': { backgroundColor: 'rgba(255, 255, 255, 0.3)' },
                  marginLeft: '8px',
                }}
              >
                <X size={20} />
              </IconButton>
            </Box>
          </Box>
        </DialogTitle>

        {/* ==================== MODAL BODY CONTENT ==================== */}
        <DialogContent
          id="printable-student-profile"
          sx={{
            padding: '28px 32px',
            backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
            color: isDark ? '#FFFFFF' : '#1E293B',
            overflowY: 'auto',
          }}
        >
          <Grid container spacing={3.5}>
            {/* ==================== SECTION 1: PERSONAL DETAILS ==================== */}
            <Grid item xs={12}>
              <Paper
                elevation={0}
                sx={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: isDark ? '#182232' : '#FFFFFF',
                  border: `1px solid ${isDark ? '#334155' : '#E2EBF6'}`,
                  boxShadow: isDark ? 'none' : '0 4px 20px rgba(11, 61, 145, 0.04)',
                }}
              >
                <SectionHeader title="SECTION 1: PERSONAL DETAILS" icon={<User size={20} />} />

                <Grid container spacing={2.5}>
                  {/* Photo & Main Identity Column */}
                  <Grid item xs={12} md={3} sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                    <Box
                      sx={{
                        width: 140,
                        height: 160,
                        borderRadius: '16px',
                        border: `2px dashed ${isDark ? '#38BDF8' : '#0B3D91'}`,
                        backgroundColor: isDark ? '#0F172A' : '#F0F9FF',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        padding: '12px',
                        textAlign: 'center',
                        gap: '8px',
                      }}
                    >
                      <Avatar
                        sx={{
                          width: 80,
                          height: 80,
                          backgroundColor: isDark ? '#38BDF8' : '#0B3D91',
                          color: '#FFFFFF',
                          fontWeight: 700,
                          fontSize: '28px',
                        }}
                      >
                        {studentName.slice(0, 2).toUpperCase()}
                      </Avatar>
                      <Typography variant="caption" sx={{ fontWeight: 600, color: isDark ? '#94A3B8' : '#64748B', fontSize: '11px' }}>
                        Student Photo
                      </Typography>
                    </Box>
                  </Grid>

                  {/* Personal Fields Grid */}
                  <Grid item xs={12} md={9}>
                    <Grid container spacing={2}>
                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Application Number" value={s.personal.applicationNumber} icon={<Hash size={14} />} />
                      </Grid>
                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Register Number" value={s.personal.registerNumber} icon={<Hash size={14} />} />
                      </Grid>
                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Student Full Name" value={s.personal.studentName} icon={<User size={14} />} />
                      </Grid>

                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Date of Birth" value={formatDateDisplay(s.personal.dateOfBirth)} icon={<Calendar size={14} />} />
                      </Grid>
                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Age" value={s.personal.age ? `${s.personal.age} Years` : 'N/A'} icon={<User size={14} />} />
                      </Grid>
                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Gender" value={s.personal.gender} icon={<User size={14} />} />
                      </Grid>

                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Aadhaar Number" value={s.personal.aadhaarNumber} icon={<Shield size={14} />} />
                      </Grid>
                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Nationality" value={s.personal.nationality} icon={<MapPin size={14} />} />
                      </Grid>
                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Community" value={s.personal.community || s.personal.caste} icon={<Users size={14} />} />
                      </Grid>

                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Caste" value={s.personal.caste} icon={<Users size={14} />} />
                      </Grid>
                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Religion" value={s.personal.religion || 'Hindu'} icon={<Heart size={14} />} />
                      </Grid>
                      <Grid item xs={12} sm={6} md={4}>
                        <DetailField label="Blood Group" value={s.personal.bloodGroup || 'O+'} icon={<Heart size={14} />} />
                      </Grid>

                      <Grid item xs={12} sm={6} md={6}>
                        <DetailField label="Mobile Number" value={s.personal.mobileNumber || s.communication?.permanentAddress?.mobileNumber} icon={<Phone size={14} />} />
                      </Grid>
                      <Grid item xs={12} sm={6} md={6}>
                        <DetailField label="Email ID" value={s.personal.email || s.communication?.permanentAddress?.email} icon={<Mail size={14} />} />
                      </Grid>
                    </Grid>
                  </Grid>
                </Grid>
              </Paper>
            </Grid>

            {/* ==================== SECTION 2: PARENT DETAILS ==================== */}
            <Grid item xs={12} md={6}>
              <Paper
                elevation={0}
                sx={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: isDark ? '#182232' : '#FFFFFF',
                  border: `1px solid ${isDark ? '#334155' : '#E2EBF6'}`,
                  height: '100%',
                }}
              >
                <SectionHeader title="SECTION 2: PARENT DETAILS" icon={<Users size={20} />} />

                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: isDark ? '#38BDF8' : '#0B3D91', mb: 1, fontSize: '13px' }}>
                      👨 Father Details
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Father Name" value={s.parent.fatherName} icon={<User size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Parent Mobile Number" value={s.parent.fatherMobile || s.parent.parentMobile} icon={<Phone size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Occupation" value={s.parent.fatherOccupation} icon={<BookOpen size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Annual Income" value={s.parent.annualIncome ? `₹ ${s.parent.annualIncome.toLocaleString('en-IN')}` : 'N/A'} icon={<CreditCard size={14} />} />
                  </Grid>

                  <Grid item xs={12} sx={{ mt: 1 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: isDark ? '#38BDF8' : '#0B3D91', mb: 1, fontSize: '13px' }}>
                      👩 Mother Details
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Mother Name" value={s.parent.motherName || 'Mrs. ' + (s.parent.fatherName ? s.parent.fatherName.split(' ')[0] : 'Mother')} icon={<User size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Mother Mobile Number" value={s.parent.motherMobile || s.parent.fatherMobile} icon={<Phone size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Occupation" value={s.parent.motherOccupation || 'Homemaker'} icon={<BookOpen size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Parent Email" value={s.parent.parentEmail || s.communication?.permanentAddress?.email} icon={<Mail size={14} />} />
                  </Grid>
                </Grid>
              </Paper>
            </Grid>

            {/* ==================== SECTION 3: COMMUNICATION DETAILS ==================== */}
            <Grid item xs={12} md={6}>
              <Paper
                elevation={0}
                sx={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: isDark ? '#182232' : '#FFFFFF',
                  border: `1px solid ${isDark ? '#334155' : '#E2EBF6'}`,
                  height: '100%',
                }}
              >
                <SectionHeader title="SECTION 3: COMMUNICATION DETAILS" icon={<MapPin size={20} />} />

                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <DetailField label="Permanent Address" value={s.communication.permanentAddress.addressLine} icon={<MapPin size={14} />} />
                  </Grid>
                  <Grid item xs={12}>
                    <DetailField
                      label="Communication Address"
                      value={s.communication.sameAsPermanent ? s.communication.permanentAddress.addressLine + ' (Same as Permanent)' : s.communication.communicationAddress.addressLine}
                      icon={<MapPin size={14} />}
                    />
                  </Grid>
                  <Grid item xs={12} sm={4}>
                    <DetailField label="District" value={s.personal.district || s.communication.permanentAddress.district || 'Puducherry'} icon={<MapPin size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={4}>
                    <DetailField label="State" value={s.communication.permanentAddress.state || 'Puducherry'} icon={<MapPin size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={4}>
                    <DetailField label="Pincode" value={s.communication.permanentAddress.pinCode} icon={<Hash size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Mobile Number" value={s.communication.permanentAddress.mobileNumber} icon={<Phone size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Email ID" value={s.communication.permanentAddress.email} icon={<Mail size={14} />} />
                  </Grid>
                </Grid>
              </Paper>
            </Grid>

            {/* ==================== SECTION 4: ADMISSION DETAILS ==================== */}
            <Grid item xs={12}>
              <Paper
                elevation={0}
                sx={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: isDark ? '#182232' : '#FFFFFF',
                  border: `1px solid ${isDark ? '#334155' : '#E2EBF6'}`,
                }}
              >
                <SectionHeader title="SECTION 4: ADMISSION DETAILS" icon={<GraduationCap size={20} />} />

                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Admission Number" value={s.personal.applicationNumber} icon={<Hash size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Admission Type" value={s.academic.admissionCategory} icon={<Award size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Academic Year" value={s.academic.batch} icon={<Calendar size={14} />} />
                  </Grid>

                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Department" value={s.academic.department} icon={<BookOpen size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Programme" value={s.academic.program} icon={<GraduationCap size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Semester" value={s.academic.semester || 'Semester I'} icon={<Calendar size={14} />} />
                  </Grid>

                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Joining Date" value={formatDateDisplay(s.academic.dateOfAdmission)} icon={<Calendar size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Counselling Rank" value={s.academic.counsellingRank || (s.academic.admissionCategory === 'CENTAC' ? '1420' : 'N/A')} icon={<Award size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField
                      label="First Graduate"
                      value={s.academic.firstGraduate ? 'Yes (Eligible)' : 'No'}
                      icon={<Award size={14} />}
                    />
                  </Grid>

                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField
                      label="Scholarship"
                      value={s.academic.scholarship ? 'Yes (Merit Scholarship)' : 'None'}
                      icon={<Award size={14} />}
                    />
                  </Grid>
                </Grid>
              </Paper>
            </Grid>

            {/* ==================== SECTION 5: ACADEMIC DETAILS ==================== */}
            <Grid item xs={12}>
              <Paper
                elevation={0}
                sx={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: isDark ? '#182232' : '#FFFFFF',
                  border: `1px solid ${isDark ? '#334155' : '#E2EBF6'}`,
                }}
              >
                <SectionHeader title="SECTION 5: ACADEMIC DETAILS" icon={<BookOpen size={20} />} />

                <Grid container spacing={3}>
                  {/* 10th SSLC Details */}
                  <Grid item xs={12} md={6}>
                    <Box sx={{ p: 2, borderRadius: '12px', border: `1px solid ${isDark ? '#334155' : '#E2E8F0'}`, backgroundColor: isDark ? '#0F172A' : '#FAF9FF' }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 700, color: isDark ? '#38BDF8' : '#0B3D91', mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Award size={16} /> 10th (SSLC) Details
                      </Typography>
                      <Grid container spacing={1.5}>
                        <Grid item xs={6}>
                          <DetailField label="Board" value={s.qualifyingExam?.sslcBoard || 'State Board'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="School Name" value={s.qualifyingExam?.sslcSchoolName || s.qualifyingExam?.institutionName || 'St. Joseph Higher Secondary School'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="Register Number" value={s.qualifyingExam?.sslcRegisterNumber || '10th-REG-981'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="Year of Passing" value={s.qualifyingExam?.monthYearPassing || 'Mar 2021'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="Marks" value={s.qualifyingExam?.sslcMarksObtained ? `${s.qualifyingExam.sslcMarksObtained} / ${s.qualifyingExam.sslcTotalMarks || 500}` : '460 / 500'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="Percentage" value={s.qualifyingExam?.sslcPercentage ? `${s.qualifyingExam.sslcPercentage}%` : '92%'} />
                        </Grid>
                      </Grid>
                    </Box>
                  </Grid>

                  {/* 12th HSC Details */}
                  <Grid item xs={12} md={6}>
                    <Box sx={{ p: 2, borderRadius: '12px', border: `1px solid ${isDark ? '#334155' : '#E2E8F0'}`, backgroundColor: isDark ? '#0F172A' : '#FAF9FF' }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 700, color: isDark ? '#38BDF8' : '#0B3D91', mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Award size={16} /> 12th (HSC) Details
                      </Typography>
                      <Grid container spacing={1.5}>
                        <Grid item xs={6}>
                          <DetailField label="Board" value={s.qualifyingExam?.hscBoard || s.qualifyingExam?.examinationPassed || 'HSC State Board'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="School Name" value={s.qualifyingExam?.hscSchoolName || s.qualifyingExam?.institutionName || 'St. Joseph HSS'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="Register Number" value={s.qualifyingExam?.hscRegisterNumber || '12th-REG-402'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="Year of Passing" value={s.qualifyingExam?.monthYearPassing || 'Mar 2023'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="Marks" value={s.hscMarks?.totalMarksObtained ? `${s.hscMarks.totalMarksObtained} / ${s.hscMarks.totalMaxMarks}` : '540 / 600'} />
                        </Grid>
                        <Grid item xs={6}>
                          <DetailField label="Percentage" value={s.hscMarks?.overallPercentage ? `${s.hscMarks.overallPercentage}%` : '90%'} />
                        </Grid>
                      </Grid>
                    </Box>
                  </Grid>

                  {/* Diploma Details (if Lateral Entry) */}
                  {(s.academic.program?.includes('Lateral Entry') || s.diplomaDetails) && (
                    <Grid item xs={12}>
                      <Box sx={{ p: 2, borderRadius: '12px', border: `1px solid ${isDark ? '#334155' : '#E2E8F0'}`, backgroundColor: isDark ? '#0F172A' : '#FAF9FF' }}>
                        <Typography variant="subtitle2" sx={{ fontWeight: 700, color: isDark ? '#38BDF8' : '#0B3D91', mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Award size={16} /> Diploma Details (Lateral Entry)
                        </Typography>
                        <Grid container spacing={1.5}>
                          <Grid item xs={12} sm={4}>
                            <DetailField label="Diploma Course" value={s.diplomaDetails?.diplomaCourse || 'Diploma in Computer Engg'} />
                          </Grid>
                          <Grid item xs={12} sm={4}>
                            <DetailField label="Institution" value={s.diplomaDetails?.institutionName || 'Motilal Nehru Polytechnic'} />
                          </Grid>
                          <Grid item xs={12} sm={4}>
                            <DetailField label="Board" value={s.diplomaDetails?.board || 'DOTE'} />
                          </Grid>
                          <Grid item xs={6} sm={3}>
                            <DetailField label="Reg Number" value={s.diplomaDetails?.registerNumber || 'DIP-2023-01'} />
                          </Grid>
                          <Grid item xs={6} sm={3}>
                            <DetailField label="Year of Passing" value={s.diplomaDetails?.monthYearPassing || 'May 2023'} />
                          </Grid>
                          <Grid item xs={6} sm={3}>
                            <DetailField label="2nd Year %" value={s.diplomaDetails?.secondYearPercentage ? `${s.diplomaDetails.secondYearPercentage}%` : '88%'} />
                          </Grid>
                          <Grid item xs={6} sm={3}>
                            <DetailField label="Aggregate %" value={s.diplomaDetails?.aggregatePercentage ? `${s.diplomaDetails.aggregatePercentage}%` : '90%'} />
                          </Grid>
                        </Grid>
                      </Box>
                    </Grid>
                  )}

                  {/* Cut-Off & Marks Table Summary */}
                  <Grid item xs={12}>
                    <Box sx={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                      <DetailField label="Engineering Cut-Off Mark" value={s.hscMarks?.engineeringCutOff || s.fee.cutOffMark ? `${s.hscMarks?.engineeringCutOff || s.fee.cutOffMark} / 200` : '182.50 / 200'} icon={<Award size={14} />} />
                      <DetailField label="Overall Percentage" value={s.hscMarks?.overallPercentage ? `${s.hscMarks.overallPercentage}%` : '91.00%'} icon={<Award size={14} />} />
                    </Box>
                  </Grid>
                </Grid>
              </Paper>
            </Grid>

            {/* ==================== SECTION 6: FEE DETAILS ==================== */}
            <Grid item xs={12}>
              <Paper
                elevation={0}
                sx={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: isDark ? '#182232' : '#FFFFFF',
                  border: `1px solid ${isDark ? '#334155' : '#E2EBF6'}`,
                }}
              >
                <SectionHeader title="SECTION 6: FEE DETAILS" icon={<CreditCard size={20} />} />

                <Grid container spacing={2.5}>
                  <Grid item xs={12} sm={6} md={3}>
                    <DetailField label="Tuition Fee (Per Year)" value={`₹ ${tuitionFee.toLocaleString('en-IN')}`} icon={<CreditCard size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <DetailField label="Bus Fee (Per Year)" value={`₹ ${busFee.toLocaleString('en-IN')}`} icon={<Bus size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <DetailField label="Hostel Fee (Per Year)" value={`₹ ${hostelFee.toLocaleString('en-IN')}`} icon={<Home size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <DetailField label="Total Fee (Grand Total)" value={`₹ ${totalFee.toLocaleString('en-IN')}`} icon={<CreditCard size={14} />} />
                  </Grid>

                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Paid Amount" value={`₹ ${paidAmount.toLocaleString('en-IN')}`} icon={<CheckCircle size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={4}>
                    <DetailField label="Pending Amount" value={`₹ ${pendingAmount.toLocaleString('en-IN')}`} icon={<XCircle size={14} />} />
                  </Grid>
                  <Grid item xs={12} sm={6} md={4}>
                    <Box
                      sx={{
                        backgroundColor: isDark ? '#0F172A' : '#F8FAFC',
                        border: `1px solid ${isDark ? '#334155' : '#E2E8F0'}`,
                        borderRadius: '12px',
                        padding: '12px 16px',
                      }}
                    >
                      <Typography variant="caption" sx={{ fontWeight: 600, fontSize: '11.5px', color: isDark ? '#94A3B8' : '#64748B', letterSpacing: '0.04em', textTransform: 'uppercase' }}>
                        Payment Status
                      </Typography>
                      <Box sx={{ mt: 0.5 }}>
                        <Chip
                          label={paymentStatus.toUpperCase()}
                          color={paymentStatus === 'Paid' ? 'success' : paymentStatus === 'Partial' ? 'warning' : 'error'}
                          sx={{ fontWeight: 700, fontSize: '12px' }}
                        />
                      </Box>
                    </Box>
                  </Grid>

                  {/* Receipt History Table */}
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: isDark ? '#38BDF8' : '#0B3D91', mt: 1, mb: 1.5, fontSize: '13px' }}>
                      🧾 Receipt History
                    </Typography>
                    <Box sx={{ overflowX: 'auto', border: `1px solid ${isDark ? '#334155' : '#E2E8F0'}`, borderRadius: '10px' }}>
                      <Table size="small">
                        <TableHead sx={{ backgroundColor: isDark ? '#0F172A' : '#F1F5F9' }}>
                          <TableRow>
                            <TableCell sx={{ fontWeight: 700, color: isDark ? '#CBD5E1' : '#334155' }}>Receipt No</TableCell>
                            <TableCell sx={{ fontWeight: 700, color: isDark ? '#CBD5E1' : '#334155' }}>Date</TableCell>
                            <TableCell sx={{ fontWeight: 700, color: isDark ? '#CBD5E1' : '#334155' }}>Payment Mode</TableCell>
                            <TableCell sx={{ fontWeight: 700, color: isDark ? '#CBD5E1' : '#334155' }}>Amount Paid</TableCell>
                            <TableCell align="center" sx={{ fontWeight: 700, color: isDark ? '#CBD5E1' : '#334155' }}>Status</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {receiptHistory.map((rec, idx) => (
                            <TableRow key={idx} hover sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                              <TableCell sx={{ fontWeight: 600, color: isDark ? '#38BDF8' : '#0B3D91' }}>{rec.receiptNo}</TableCell>
                              <TableCell sx={{ color: isDark ? '#F8FAFC' : '#1E293B' }}>{formatDateDisplay(rec.date)}</TableCell>
                              <TableCell sx={{ color: isDark ? '#F8FAFC' : '#1E293B' }}>{rec.mode}</TableCell>
                              <TableCell sx={{ fontWeight: 600, color: isDark ? '#F8FAFC' : '#1E293B' }}>₹ {rec.amount.toLocaleString('en-IN')}</TableCell>
                              <TableCell align="center">
                                <Chip label={rec.status} color="success" size="small" variant="outlined" sx={{ fontWeight: 600, height: '22px' }} />
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </Box>
                  </Grid>
                </Grid>
              </Paper>
            </Grid>

            {/* ==================== SECTION 7 & 8: BUS & HOSTEL DETAILS ==================== */}
            <Grid item xs={12} md={6}>
              <Paper
                elevation={0}
                sx={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: isDark ? '#182232' : '#FFFFFF',
                  border: `1px solid ${isDark ? '#334155' : '#E2EBF6'}`,
                  height: '100%',
                }}
              >
                <SectionHeader title="SECTION 7: BUS DETAILS" icon={<Bus size={20} />} />

                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6}>
                    <DetailField
                      label="Bus Required"
                      value={
                        <Chip
                          label={s.fee.busRouteSelected || s.fee.busTransportRequired ? 'YES' : 'NO'}
                          color={s.fee.busRouteSelected || s.fee.busTransportRequired ? 'primary' : 'default'}
                          size="small"
                          sx={{ fontWeight: 700 }}
                        />
                      }
                      icon={<Bus size={14} />}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Bus Fee" value={`₹ ${busFee.toLocaleString('en-IN')}`} icon={<CreditCard size={14} />} />
                  </Grid>
                  <Grid item xs={12}>
                    <DetailField label="Route" value={s.fee.busRouteSelected || 'Route 1: Puducherry Town - College'} icon={<MapPin size={14} />} />
                  </Grid>
                  <Grid item xs={12}>
                    <DetailField label="Bus Stop" value={s.fee.busStopSelected || 'Muthialpet Clock Tower'} icon={<MapPin size={14} />} />
                  </Grid>
                </Grid>
              </Paper>
            </Grid>

            <Grid item xs={12} md={6}>
              <Paper
                elevation={0}
                sx={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: isDark ? '#182232' : '#FFFFFF',
                  border: `1px solid ${isDark ? '#334155' : '#E2EBF6'}`,
                  height: '100%',
                }}
              >
                <SectionHeader title="SECTION 8: HOSTEL DETAILS" icon={<Home size={20} />} />

                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6}>
                    <DetailField
                      label="Hostel Required"
                      value={
                        <Chip
                          label={s.fee.hostelRequired ? 'YES' : 'NO'}
                          color={s.fee.hostelRequired ? 'primary' : 'default'}
                          size="small"
                          sx={{ fontWeight: 700 }}
                        />
                      }
                      icon={<Home size={14} />}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DetailField label="Hostel Fee" value={`₹ ${hostelFee.toLocaleString('en-IN')}`} icon={<CreditCard size={14} />} />
                  </Grid>
                </Grid>
              </Paper>
            </Grid>

            {/* ==================== SECTION 9: CERTIFICATES ==================== */}
            <Grid item xs={12}>
              <Paper
                elevation={0}
                sx={{
                  padding: '24px',
                  borderRadius: '16px',
                  backgroundColor: isDark ? '#182232' : '#FFFFFF',
                  border: `1px solid ${isDark ? '#334155' : '#E2EBF6'}`,
                }}
              >
                <SectionHeader title="SECTION 9: CERTIFICATES & DOCUMENTS" icon={<FileCheck size={20} />} />

                <Grid container spacing={2}>
                  {s.certificates && s.certificates.length > 0 ? (
                    s.certificates.map((cert) => (
                      <Grid item xs={12} sm={6} md={4} key={cert.id}>
                        <Paper
                          variant="outlined"
                          sx={{
                            padding: '14px 16px',
                            borderRadius: '12px',
                            backgroundColor: isDark ? '#0F172A' : '#F8FAFC',
                            borderColor: isDark ? '#334155' : '#E2E8F0',
                            display: 'flex',
                            flexDirection: 'column',
                            justifyContent: 'space-between',
                            height: '100%',
                            gap: '12px',
                          }}
                        >
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '8px' }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                              <FileCheck size={20} color={cert.received ? '#16A34A' : '#94A3B8'} />
                              <Box>
                                <Typography variant="body2" sx={{ fontWeight: 700, color: isDark ? '#F8FAFC' : '#1E293B', fontSize: '13.5px' }}>
                                  {cert.name}
                                </Typography>
                                <Typography variant="caption" sx={{ color: isDark ? '#94A3B8' : '#64748B', fontSize: '11px', display: 'block' }}>
                                  {cert.fileName || `${cert.name.toLowerCase().replace(/[^a-z0-9]/g, '_')}_${appNo}.pdf`}
                                </Typography>
                              </Box>
                            </Box>
                            <Chip
                              label={cert.received ? 'Received' : 'Not Received'}
                              color={cert.received ? 'success' : 'default'}
                              size="small"
                              sx={{ fontWeight: 700, fontSize: '10.5px', height: '20px' }}
                            />
                          </Box>

                          <Box sx={{ display: 'flex', gap: '8px', mt: '4px' }}>
                            <Button
                              variant="outlined"
                              size="small"
                              startIcon={<Eye size={14} />}
                              onClick={() => setPreviewCert(cert)}
                              sx={{
                                flex: 1,
                                height: '32px',
                                fontSize: '12px',
                                borderRadius: '8px',
                                textTransform: 'none',
                                fontWeight: 600,
                              }}
                            >
                              Preview
                            </Button>
                            <Button
                              variant="contained"
                              size="small"
                              startIcon={<Download size={14} />}
                              onClick={() => handleDownloadCert(cert)}
                              sx={{
                                flex: 1,
                                height: '32px',
                                fontSize: '12px',
                                borderRadius: '8px',
                                textTransform: 'none',
                                fontWeight: 600,
                                backgroundColor: isDark ? '#38BDF8' : '#0B3D91',
                                color: isDark ? '#0F172A' : '#FFFFFF',
                              }}
                            >
                              Download
                            </Button>
                          </Box>
                        </Paper>
                      </Grid>
                    ))
                  ) : (
                    <Grid item xs={12}>
                      <Typography variant="body2" sx={{ color: isDark ? '#94A3B8' : '#64748B', fontStyle: 'italic' }}>
                        No uploaded certificates found.
                      </Typography>
                    </Grid>
                  )}
                </Grid>
              </Paper>
            </Grid>
          </Grid>
        </DialogContent>

        {/* ==================== BOTTOM BUTTONS ==================== */}
        <DialogActions
          sx={{
            padding: '16px 28px',
            backgroundColor: isDark ? '#0F172A' : '#F8FAFC',
            borderTop: `1px solid ${isDark ? '#334155' : '#E2E8F0'}`,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <Button
            variant="outlined"
            onClick={closeViewModal}
            sx={{
              height: '42px',
              padding: '0 24px',
              borderRadius: '10px',
              fontWeight: 600,
              fontSize: '14px',
              color: isDark ? '#CBD5E1' : '#475569',
              borderColor: isDark ? '#334155' : '#CBD5E1',
            }}
          >
            Close
          </Button>

          <Box sx={{ display: 'flex', gap: '14px' }}>
            <Button
              variant="outlined"
              startIcon={<Printer size={18} />}
              onClick={handlePrint}
              sx={{
                height: '42px',
                padding: '0 22px',
                borderRadius: '10px',
                fontWeight: 600,
                fontSize: '14px',
                color: isDark ? '#38BDF8' : '#0B3D91',
                borderColor: isDark ? '#38BDF8' : '#0B3D91',
                '&:hover': {
                  backgroundColor: isDark ? 'rgba(56, 189, 248, 0.1)' : 'rgba(11, 61, 145, 0.05)',
                },
              }}
            >
              Print Student Profile
            </Button>

            <Button
              variant="contained"
              startIcon={<Download size={18} />}
              onClick={() => generateStudentPdf(s)}
              sx={{
                height: '42px',
                padding: '0 24px',
                borderRadius: '10px',
                fontWeight: 600,
                fontSize: '14px',
                backgroundColor: '#16A34A',
                color: '#FFFFFF',
                boxShadow: '0 4px 12px rgba(22, 163, 74, 0.25)',
                '&:hover': {
                  backgroundColor: '#15803D',
                },
              }}
            >
              Export PDF
            </Button>
          </Box>
        </DialogActions>
      </Dialog>

      {/* ==================== CERTIFICATE PREVIEW DIALOG ==================== */}
      {previewCert && (
        <Dialog
          open={Boolean(previewCert)}
          onClose={() => setPreviewCert(null)}
          maxWidth="md"
          fullWidth
          PaperProps={{
            sx: {
              borderRadius: '16px',
              backgroundColor: isDark ? '#1E293B' : '#FFFFFF',
              color: isDark ? '#FFFFFF' : '#1E293B',
              p: 2,
            },
          }}
        >
          <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              📄 Document Preview: {previewCert.name}
            </Typography>
            <IconButton onClick={() => setPreviewCert(null)}>
              <X size={18} />
            </IconButton>
          </DialogTitle>
          <DialogContent>
            <Box
              sx={{
                height: 400,
                borderRadius: '12px',
                border: `2px dashed ${isDark ? '#334155' : '#CBD5E1'}`,
                backgroundColor: isDark ? '#0F172A' : '#F1F5F9',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 2,
                p: 3,
                textAlign: 'center',
              }}
            >
              <FileCheck size={64} color={isDark ? '#38BDF8' : '#0B3D91'} />
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                {previewCert.name}
              </Typography>
              <Typography variant="body2" sx={{ color: isDark ? '#94A3B8' : '#64748B' }}>
                File: {previewCert.fileName || `${previewCert.name.toLowerCase().replace(/[^a-z0-9]/g, '_')}_${appNo}.pdf`}
              </Typography>
              <Chip
                label={previewCert.received ? 'Verified & Received' : 'Not Received'}
                color={previewCert.received ? 'success' : 'default'}
                sx={{ fontWeight: 700 }}
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setPreviewCert(null)}>Close</Button>
            <Button
              variant="contained"
              startIcon={<Download size={16} />}
              onClick={() => {
                handleDownloadCert(previewCert);
                setPreviewCert(null);
              }}
            >
              Download File
            </Button>
          </DialogActions>
        </Dialog>
      )}
    </>
  );
};
