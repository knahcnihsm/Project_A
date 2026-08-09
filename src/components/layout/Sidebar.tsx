import React, { useState } from 'react';
import {
  Box,
  Typography,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Collapse,
} from '@mui/material';
import {
  LayoutDashboard,
  UserPlus,
  Pencil,
  Archive,
  FileSpreadsheet,
  CheckCircle2,
  ArrowLeft,
  ChevronDown,
  ChevronRight,
  FileText,
  User,
  Users,
  MapPin,
  GraduationCap,
  CreditCard,
  ListChecks,
} from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAdmission } from '../../context/AdmissionContext';
import { exportStudentsToExcel } from '../../utils/exportExcel';
import { generateStudentPdf } from '../../utils/exportPdf';
import { useThemeContext } from '../../context/ThemeContext';

export const Sidebar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { mode } = useThemeContext();
  const isDark = mode === 'dark';

  const {
    activeStep,
    setActiveStep,
    selectedStudentIds,
    students,
    startEditStudent,
    startAddAdmission,
    draftStudent,
    showWarningModal,
  } = useAdmission();

  const [exportOpen, setExportOpen] = useState<boolean>(true);
  const [academicMenuOpen, setAcademicMenuOpen] = useState<boolean>(true);

  const isAdmissionRoute =
    location.pathname.startsWith('/admission') ||
    location.pathname.startsWith('/EditStudent');

  const handleEditClick = () => {
    if (selectedStudentIds.length === 1) {
      const studentToEdit = students.find((s) => s.id === selectedStudentIds[0]);
      if (studentToEdit) {
        startEditStudent(studentToEdit);
        navigate('/EditStudent');
      }
    } else if (selectedStudentIds.length === 0) {
      showWarningModal(
        'No Student Selected',
        'Please select exactly one student to edit.'
      );
    } else {
      showWarningModal(
        'Multiple Students Selected',
        'You can edit only one student at a time.'
      );
    }
  };

  const handleAddAdmissionClick = () => {
    startAddAdmission();
    navigate('/admission');
  };

  // 1. Dashboard active/inactive style
  const navItemStyle = (isActive: boolean) => ({
    borderRadius: '8px',
    marginBottom: '6px',
    padding: '8px 12px',
    backgroundColor: isActive ? 'rgba(255, 255, 255, 0.95)' : 'transparent', // White background pill
    color: isActive ? '#0B3D91' : '#FFFFFF', // Navy blue text for active
    transition: 'all 180ms ease-in-out',
    '&:hover': {
      backgroundColor: isActive ? 'rgba(255, 255, 255, 0.98)' : 'rgba(255, 255, 255, 0.12)',
    },
  });

  // 2. Wizard active step style
  const stepNavItemStyle = (isActive: boolean) => ({
    borderRadius: '8px',
    marginBottom: '6px',
    padding: '8px 12px',
    backgroundColor: isActive ? '#1A73E8' : 'transparent', // Light Blue Active Background
    color: '#FFFFFF', // White Text
    transition: 'all 180ms ease-in-out',
    '&:hover': {
      backgroundColor: isActive ? '#1565C0' : 'rgba(255, 255, 255, 0.12)',
    },
  });

  // 3. Sub-menu item style
  const subNavItemStyle = (isActive: boolean) => ({
    borderRadius: '6px',
    marginBottom: '4px',
    padding: '6px 12px 6px 36px',
    backgroundColor: isActive ? 'rgba(255, 255, 255, 0.15)' : 'transparent', // Slightly lighter blue background
    color: '#FFFFFF', // White text
    borderLeft: isActive ? '3px solid #38BDF8' : 'none', // Blue left indicator
    transition: 'all 180ms ease-in-out',
    '&:hover': {
      backgroundColor: isActive ? 'rgba(255, 255, 255, 0.2)' : 'rgba(255, 255, 255, 0.08)',
    },
  });

  const program = draftStudent.academic?.program || '';

  const getSubMenuSteps = () => {
    if (!program) return [];
    if (program === 'First Year B.Tech') {
      return [
        { label: 'Admission Details', stepId: 3 },
        { label: 'Qualifying Examination', stepId: 4 },
        { label: 'HSC Marks', stepId: 4 },
      ];
    } else if (program === 'Second Year B.Tech (Lateral Entry)') {
      return [
        { label: 'Admission Details', stepId: 3 },
        { label: 'Diploma Details', stepId: 4 },
      ];
    } else if (program === 'PG') {
      return [
        { label: 'Admission Details', stepId: 3 },
        { label: 'UG / Qualifying Degree', stepId: 4 },
      ];
    }
    return [];
  };

  return (
    <Box
      component="aside"
      sx={{
        width: '240px',
        minWidth: '240px',
        height: 'calc(100vh - 64px)',
        position: 'sticky',
        top: '64px',
        background: isDark
          ? '#000000'
          : 'linear-gradient(180deg, #0A2D6E 0%, #0B3D91 55%, #1565C0 100%)', // Original blue gradient
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        color: '#FFFFFF',
        boxShadow: '4px 0 20px rgba(0, 0, 0, 0.15)',
        borderRight: isDark ? '1px solid #222222' : 'none',
        overflowY: 'hidden', // Disabled vertical scrollbar
        zIndex: 1000,
      }}
    >
      {/* Navigation Menu */}
      <Box sx={{ padding: '8px 8px', flexGrow: 1 }}>
        {isAdmissionRoute ? (
          /* Step Wizard Navigation Mode (Dashboard-Style Design) */
          <Box>
            {/* Back to Dashboard Button (Styled like Dashboard item) */}
            <ListItemButton
              onClick={() => navigate('/')}
              sx={{
                borderRadius: '8px',
                marginBottom: '16px',
                padding: '8px 12px',
                color: '#FFFFFF',
                '&:hover': {
                  backgroundColor: 'rgba(255, 255, 255, 0.12)',
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                <ArrowLeft size={17} />
              </ListItemIcon>
              <ListItemText
                primary="BACK TO DASHBOARD"
                primaryTypographyProps={{
                  fontSize: '11.5px',
                  fontWeight: 700,
                  letterSpacing: '0.04em',
                  color: '#FFFFFF',
                }}
              />
            </ListItemButton>

            <List disablePadding>
              {/* 1. Student Details */}
              <ListItemButton
                onClick={() => setActiveStep(0)}
                sx={stepNavItemStyle(activeStep === 0)}
              >
                <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                  {activeStep > 0 ? <CheckCircle2 size={17} color="#38BDF8" /> : <User size={17} />}
                </ListItemIcon>
                <ListItemText
                  primary="STUDENT DETAILS"
                  primaryTypographyProps={{
                    fontSize: '11.5px',
                    fontWeight: activeStep === 0 ? 700 : 600,
                    letterSpacing: '0.04em',
                    color: 'inherit',
                  }}
                />
              </ListItemButton>

              {/* 2. Parent Details */}
              <ListItemButton
                onClick={() => setActiveStep(1)}
                sx={stepNavItemStyle(activeStep === 1)}
              >
                <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                  {activeStep > 1 ? <CheckCircle2 size={17} color="#38BDF8" /> : <Users size={17} />}
                </ListItemIcon>
                <ListItemText
                  primary="PARENT DETAILS"
                  primaryTypographyProps={{
                    fontSize: '11.5px',
                    fontWeight: activeStep === 1 ? 700 : 600,
                    letterSpacing: '0.04em',
                    color: 'inherit',
                  }}
                />
              </ListItemButton>

              {/* 3. Communication */}
              <ListItemButton
                onClick={() => setActiveStep(2)}
                sx={stepNavItemStyle(activeStep === 2)}
              >
                <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                  {activeStep > 2 ? <CheckCircle2 size={17} color="#38BDF8" /> : <MapPin size={17} />}
                </ListItemIcon>
                <ListItemText
                  primary="COMMUNICATION"
                  primaryTypographyProps={{
                    fontSize: '11.5px',
                    fontWeight: activeStep === 2 ? 700 : 600,
                    letterSpacing: '0.04em',
                    color: 'inherit',
                  }}
                />
              </ListItemButton>

              {/* 4. Academic Details (Expandable) */}
              <Box>
                <ListItemButton
                  onClick={() => {
                    setAcademicMenuOpen(!academicMenuOpen);
                    setActiveStep(3);
                  }}
                  sx={stepNavItemStyle(activeStep === 3 || activeStep === 4)}
                >
                  <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                    {activeStep > 4 ? <CheckCircle2 size={17} color="#38BDF8" /> : <GraduationCap size={17} />}
                  </ListItemIcon>
                  <ListItemText
                    primary="ACADEMIC DETAILS"
                    primaryTypographyProps={{
                      fontSize: '11.5px',
                      fontWeight: (activeStep === 3 || activeStep === 4) ? 700 : 600,
                      letterSpacing: '0.04em',
                      color: 'inherit',
                    }}
                  />
                  {getSubMenuSteps().length > 0 && (
                    ((activeStep === 3 || activeStep === 4) || academicMenuOpen) ? <ChevronDown size={14} /> : <ChevronRight size={14} />
                  )}
                </ListItemButton>

                <Collapse in={((activeStep === 3 || activeStep === 4) || academicMenuOpen) && getSubMenuSteps().length > 0} timeout="auto" unmountOnExit>
                  <List disablePadding>
                    {getSubMenuSteps().map((subStep, index) => {
                      let isSubActive = false;
                      if (subStep.stepId === 3 && activeStep === 3) {
                        isSubActive = true;
                      } else if (subStep.stepId === 4 && activeStep === 4) {
                        isSubActive = true;
                      }

                      return (
                        <ListItemButton
                          key={index}
                          onClick={() => setActiveStep(subStep.stepId)}
                          sx={subNavItemStyle(isSubActive)}
                        >
                          <ListItemText
                            primary={subStep.label}
                            primaryTypographyProps={{
                              fontSize: '11px',
                              fontWeight: isSubActive ? 700 : 500,
                              letterSpacing: '0.02em',
                              color: '#FFFFFF',
                            }}
                          />
                        </ListItemButton>
                      );
                    })}
                  </List>
                </Collapse>
              </Box>

              {/* 5. Fee Structure */}
              <ListItemButton
                onClick={() => setActiveStep(5)}
                sx={stepNavItemStyle(activeStep === 5)}
              >
                <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                  {activeStep > 5 ? <CheckCircle2 size={17} color="#38BDF8" /> : <CreditCard size={17} />}
                </ListItemIcon>
                <ListItemText
                  primary="FEE STRUCTURE"
                  primaryTypographyProps={{
                    fontSize: '11.5px',
                    fontWeight: activeStep === 5 ? 700 : 600,
                    letterSpacing: '0.04em',
                    color: 'inherit',
                  }}
                />
              </ListItemButton>

              {/* 6. Certificates Upload */}
              <ListItemButton
                onClick={() => setActiveStep(6)}
                sx={stepNavItemStyle(activeStep === 6)}
              >
                <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                  {activeStep > 6 ? <CheckCircle2 size={17} color="#38BDF8" /> : <FileText size={17} />}
                </ListItemIcon>
                <ListItemText
                  primary="CERTIFICATES UPLOAD"
                  primaryTypographyProps={{
                    fontSize: '11.5px',
                    fontWeight: activeStep === 6 ? 700 : 600,
                    letterSpacing: '0.04em',
                    color: 'inherit',
                  }}
                />
              </ListItemButton>
            </List>
          </Box>
        ) : (
          /* Main ERP Sidebar Navigation */
          <List disablePadding>
            {/* Dashboard */}
            <ListItemButton
              onClick={() => navigate('/')}
              sx={navItemStyle(location.pathname === '/')}
            >
              <ListItemIcon
                sx={{
                  minWidth: '30px',
                  color: location.pathname === '/' ? '#0B3D91' : '#FFFFFF',
                }}
              >
                <LayoutDashboard size={17} />
              </ListItemIcon>
              <ListItemText
                primary="DASHBOARD"
                primaryTypographyProps={{
                  fontSize: '11.5px',
                  fontWeight: 700,
                  letterSpacing: '0.04em',
                  color: 'inherit',
                }}
              />
            </ListItemButton>

            {/* Add New Admission */}
            <ListItemButton
              onClick={handleAddAdmissionClick}
              sx={navItemStyle(false)}
            >
              <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                <UserPlus size={17} />
              </ListItemIcon>
              <ListItemText
                primary="ADD NEW ADMISSION"
                primaryTypographyProps={{ fontSize: '11.5px', fontWeight: 600, letterSpacing: '0.04em', color: 'inherit' }}
              />
            </ListItemButton>

            {/* Edit Student Details */}
            <ListItemButton
              onClick={handleEditClick}
              sx={navItemStyle(false)}
            >
              <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                <Pencil size={17} />
              </ListItemIcon>
              <ListItemText
                primary="EDIT STUDENT DETAILS"
                primaryTypographyProps={{ fontSize: '11.5px', fontWeight: 600, letterSpacing: '0.04em', color: 'inherit' }}
              />
            </ListItemButton>

            {/* Archive Students List */}
            <ListItemButton
              onClick={() => navigate('/archive')}
              sx={navItemStyle(location.pathname === '/archive')}
            >
              <ListItemIcon
                sx={{
                  minWidth: '30px',
                  color: location.pathname === '/archive' ? '#0B3D91' : '#FFFFFF',
                }}
              >
                <Archive size={17} />
              </ListItemIcon>
              <ListItemText
                primary="ARCHIVE STUDENTS LIST"
                primaryTypographyProps={{
                  fontSize: '11.5px',
                  fontWeight: 600,
                  letterSpacing: '0.04em',
                  color: 'inherit',
                }}
              />
            </ListItemButton>

            {/* Bulk Update */}
            <ListItemButton
              onClick={() => navigate('/bulk-update')}
              sx={navItemStyle(location.pathname === '/bulk-update')}
            >
              <ListItemIcon
                sx={{
                  minWidth: '30px',
                  color: location.pathname === '/bulk-update' ? '#0B3D91' : '#FFFFFF',
                }}
              >
                <ListChecks size={17} />
              </ListItemIcon>
              <ListItemText
                primary="BULK UPDATE"
                primaryTypographyProps={{
                  fontSize: '11.5px',
                  fontWeight: 600,
                  letterSpacing: '0.04em',
                  color: 'inherit',
                }}
              />
            </ListItemButton>

            {/* Export Section Header */}
            <Box sx={{ padding: '10px 12px 4px' }}>
              <Typography
                sx={{
                  fontSize: '10px',
                  fontWeight: 700,
                  color: 'rgba(255,255,255,0.55)',
                  letterSpacing: '0.1em',
                  textTransform: 'uppercase',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  cursor: 'pointer',
                  userSelect: 'none',
                }}
                onClick={() => setExportOpen(!exportOpen)}
              >
                EXPORT
                {exportOpen ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
              </Typography>
            </Box>

            {/* Export Submenu Items */}
            <Collapse in={exportOpen} timeout="auto" unmountOnExit>
              <List disablePadding>
                <ListItemButton
                  onClick={() => {
                    if (selectedStudentIds.length === 0) {
                      showWarningModal(
                        'No Student Selected',
                        'Please select at least one student before exporting.'
                      );
                      return;
                    }
                    const selected = students.filter((s) => selectedStudentIds.includes(s.id));
                    exportStudentsToExcel(selected, 'Exported_Selected_Students.xlsx');
                  }}
                  sx={{
                    borderRadius: '6px',
                    marginBottom: '6px',
                    padding: '6px 12px',
                    color: '#E0F2FE',
                    '&:hover': { backgroundColor: 'rgba(255,255,255,0.12)' },
                  }}
                >
                  <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                    <FileSpreadsheet size={15} />
                  </ListItemIcon>
                  <ListItemText
                    primary="EXPORT SELECTED"
                    secondary="STUDENT (EXCEL)"
                    primaryTypographyProps={{ fontSize: '10.5px', fontWeight: 600, color: '#FFFFFF', letterSpacing: '0.02em' }}
                    secondaryTypographyProps={{ fontSize: '9.5px', color: 'rgba(255,255,255,0.7)' }}
                  />
                </ListItemButton>

                <ListItemButton
                  onClick={() => {
                    if (selectedStudentIds.length === 0) {
                      showWarningModal(
                        'No Student Selected',
                        'Please select a student before exporting the PDF.'
                      );
                      return;
                    }
                    const selected = students.filter((s) => selectedStudentIds.includes(s.id));
                    selected.forEach((s) => generateStudentPdf(s));
                  }}
                  sx={{
                    borderRadius: '6px',
                    marginBottom: '6px',
                    padding: '6px 12px',
                    color: '#E0F2FE',
                    '&:hover': { backgroundColor: 'rgba(255,255,255,0.12)' },
                  }}
                >
                  <ListItemIcon sx={{ minWidth: '30px', color: '#FFFFFF' }}>
                    <FileText size={15} />
                  </ListItemIcon>
                  <ListItemText
                    primary="EXPORT STUDENT"
                    secondary="DETAILS (PDF)"
                    primaryTypographyProps={{ fontSize: '10.5px', fontWeight: 600, color: '#FFFFFF', letterSpacing: '0.02em' }}
                    secondaryTypographyProps={{ fontSize: '9.5px', color: 'rgba(255,255,255,0.7)' }}
                  />
                </ListItemButton>
              </List>
            </Collapse>
          </List>
        )}
      </Box>

      {/* Bottom Campus Building Illustration & Version (Original design) */}
      <Box
        sx={{
          position: 'relative',
          height: '170px',
          overflow: 'hidden',
          borderTop: '1px solid rgba(255, 255, 255, 0.15)',
          flexShrink: 0,
        }}
      >
        <Box
          component="img"
          src="/images/sidebar-building.png"
          alt="Rajiv Gandhi College Building"
          sx={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            position: 'absolute',
            top: 0,
            left: 0,
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            background: 'linear-gradient(180deg, rgba(8, 47, 73, 0.5) 0%, rgba(11, 61, 145, 0.9) 100%)',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'flex-end',
            padding: '16px',
          }}
        >
          <Typography variant="body1" sx={{ fontWeight: 700, fontSize: '13px', color: '#FFFFFF', lineHeight: 1.3 }}>
            Academic ERP Systems
          </Typography>
          <Typography variant="caption" sx={{ color: '#93C5FD', fontSize: '12px', fontWeight: 500 }}>
            Version 2.4.0
          </Typography>
        </Box>
      </Box>
    </Box>
  );
};
