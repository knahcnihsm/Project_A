import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  StudentRecord,
  SnackbarState,
  ConfirmDialogState,
  StudentFilterParams,
} from '../types';
import { INITIAL_STUDENTS, INITIAL_ARCHIVED_STUDENTS } from './mockData';
import { STANDARD_CERTIFICATES } from '../utils/constants';

interface AdmissionContextType {
  students: StudentRecord[];
  archivedStudents: StudentRecord[];
  selectedStudentIds: string[];
  setSelectedStudentIds: React.Dispatch<React.SetStateAction<string[]>>;

  // Active step & Draft form state
  activeStep: number;
  setActiveStep: (step: number) => void;
  draftStudent: Partial<StudentRecord>;
  setDraftStudent: React.Dispatch<React.SetStateAction<Partial<StudentRecord>>>;
  updateDraftSection: <K extends keyof StudentRecord>(section: K, value: StudentRecord[K]) => void;

  // Edit / View state
  isEditMode: boolean;
  isViewReadOnly: boolean;
  editingStudentId: string | null;
  viewModalStudent: StudentRecord | null;
  closeViewModal: () => void;
  startAddAdmission: () => void;
  startEditStudent: (student: StudentRecord) => void;
  startViewStudent: (student: StudentRecord) => void;

  // Actions
  saveCurrentAdmission: () => StudentRecord;
  archiveSelectedStudents: (reason: string) => void;
  archiveSingleStudent: (studentId: string, reason: string) => void;
  restoreStudents: (studentIds: string[]) => void;
  bulkUpdateFromRows: (updatedRows: Partial<StudentRecord>[]) => { updatedCount: number; skippedCount: number };

  // Common UI State
  snackbar: SnackbarState;
  showSnackbar: (message: string, severity?: SnackbarState['severity']) => void;
  hideSnackbar: () => void;

  confirmDialog: ConfirmDialogState;
  showConfirm: (title: string, message: string, onConfirm: () => void, confirmText?: string) => void;
  hideConfirm: () => void;

  // Bulk update modal state
  bulkUpdateModalOpen: boolean;
  setBulkUpdateModalOpen: (open: boolean) => void;
}

const AdmissionContext = createContext<AdmissionContextType | null>(null);

export const useAdmission = () => {
  const ctx = useContext(AdmissionContext);
  if (!ctx) {
    throw new Error('useAdmission must be used within AdmissionProvider');
  }
  return ctx;
};

const defaultDraft: Partial<StudentRecord> = {
  personal: {
    applicationNumber: '',
    registerNumber: '',
    studentName: '',
    dateOfBirth: '',
    age: undefined,
    aadhaarNumber: '',
    gender: undefined,
    district: '',
    nationality: 'Indian',
    caste: undefined,
  } as any,
  parent: {
    fatherName: '',
    fatherMobile: '',
    fatherOccupation: '',
    annualIncome: undefined,
  } as any,
  communication: {
    permanentAddress: {
      addressLine: '',
      pinCode: '',
      mobileNumber: '',
      email: '',
    },
    communicationAddress: {
      addressLine: '',
      pinCode: '',
      mobileNumber: '',
      email: '',
    },
    sameAsPermanent: false,
  } as any,
  academic: {
    admissionCategory: undefined,
    program: undefined,
    department: '',
    batch: '',
    dateOfAdmission: '',
  } as any,
  fee: {
    cutOffMark: undefined,
    tuitionFeePerYear: undefined,
    courseDurationYears: 4,
    totalTuitionFee: undefined,
    busTransportRequired: false,
    busRouteSelected: '',
    busStopSelected: '',
    busFee: 0,
    hostelRequired: false,
    hostelFee: 0,
    grandTotalFee: undefined,
  } as any,
  certificates: STANDARD_CERTIFICATES.map((name, i) => ({
    id: `cert-${i + 1}`,
    name,
    received: false,
  })),
};

export const AdmissionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [students, setStudents] = useState<StudentRecord[]>(() => {
    const saved = localStorage.getItem('rgcet_active_students');
    return saved ? JSON.parse(saved) : INITIAL_STUDENTS;
  });

  const [archivedStudents, setArchivedStudents] = useState<StudentRecord[]>(() => {
    const saved = localStorage.getItem('rgcet_archived_students');
    return saved ? JSON.parse(saved) : INITIAL_ARCHIVED_STUDENTS;
  });

  const [selectedStudentIds, setSelectedStudentIds] = useState<string[]>([]);
  const [activeStep, setActiveStep] = useState<number>(0);
  const [draftStudent, setDraftStudent] = useState<Partial<StudentRecord>>(defaultDraft);
  const [isEditMode, setIsEditMode] = useState<boolean>(false);
  const [isViewReadOnly, setIsViewReadOnly] = useState<boolean>(false);
  const [editingStudentId, setEditingStudentId] = useState<string | null>(null);

  const [bulkUpdateModalOpen, setBulkUpdateModalOpen] = useState<boolean>(false);

  // Notifications & Modals
  const [snackbar, setSnackbar] = useState<SnackbarState>({
    open: false,
    message: '',
    severity: 'success',
  });

  const [confirmDialog, setConfirmDialog] = useState<ConfirmDialogState>({
    open: false,
    title: '',
    message: '',
    onConfirm: () => {},
  });

  useEffect(() => {
    localStorage.setItem('rgcet_active_students', JSON.stringify(students));
  }, [students]);

  useEffect(() => {
    localStorage.setItem('rgcet_archived_students', JSON.stringify(archivedStudents));
  }, [archivedStudents]);

  const showSnackbar = (message: string, severity: SnackbarState['severity'] = 'success') => {
    setSnackbar({ open: true, message, severity });
  };

  const hideSnackbar = () => {
    setSnackbar((prev) => ({ ...prev, open: false }));
  };

  const showConfirm = (
    title: string,
    message: string,
    onConfirm: () => void,
    confirmText: string = 'Confirm'
  ) => {
    setConfirmDialog({
      open: true,
      title,
      message,
      confirmText,
      onConfirm: () => {
        onConfirm();
        hideConfirm();
      },
    });
  };

  const hideConfirm = () => {
    setConfirmDialog((prev) => ({ ...prev, open: false }));
  };

  const updateDraftSection = <K extends keyof StudentRecord>(
    section: K,
    value: StudentRecord[K]
  ) => {
    setDraftStudent((prev) => ({ ...prev, [section]: value }));
  };

  const [viewModalStudent, setViewModalStudent] = useState<StudentRecord | null>(null);

  const closeViewModal = () => {
    setViewModalStudent(null);
  };

  const startAddAdmission = () => {
    setIsEditMode(false);
    setIsViewReadOnly(false);
    setEditingStudentId(null);
    setActiveStep(0);
    setDraftStudent(JSON.parse(JSON.stringify(defaultDraft)));
  };

  const startEditStudent = (student: StudentRecord) => {
    setIsEditMode(true);
    setIsViewReadOnly(false);
    setEditingStudentId(student.id);
    setActiveStep(0);
    setDraftStudent(JSON.parse(JSON.stringify(student)));
  };

  const startViewStudent = (student: StudentRecord) => {
    setIsEditMode(false);
    setIsViewReadOnly(true);
    setEditingStudentId(student.id);
    setActiveStep(0);
    setDraftStudent(JSON.parse(JSON.stringify(student)));
    setViewModalStudent(student);
  };

  const saveCurrentAdmission = (): StudentRecord => {
    const now = new Date().toISOString();
    let savedRecord: StudentRecord;

    if (isEditMode && editingStudentId) {
      savedRecord = {
        ...(draftStudent as StudentRecord),
        id: editingStudentId,
        updatedAt: now,
      };
      setStudents((prev) =>
        prev.map((s) => (s.id === editingStudentId ? savedRecord : s))
      );
      showSnackbar('Student details updated successfully!');
    } else {
      const newId = `STU-2026-${Math.floor(100 + Math.random() * 900)}`;
      savedRecord = {
        ...(draftStudent as StudentRecord),
        id: newId,
        status: 'Active',
        createdAt: now,
        updatedAt: now,
      };
      setStudents((prev) => [savedRecord, ...prev]);
      showSnackbar('New Admission created successfully!');
    }

    startAddAdmission();
    return savedRecord;
  };

  const archiveSingleStudent = (studentId: string, reason: string) => {
    const student = students.find((s) => s.id === studentId);
    if (!student) return;

    const now = new Date().toISOString();
    const archivedItem: StudentRecord = {
      ...student,
      status: 'Archived',
      archivedAt: now,
      archivedBy: 'Admin User',
      archiveReason: reason,
      updatedAt: now,
    };

    setStudents((prev) => prev.filter((s) => s.id !== studentId));
    setArchivedStudents((prev) => [archivedItem, ...prev]);
    setSelectedStudentIds((prev) => prev.filter((id) => id !== studentId));
    showSnackbar(`Student ${student.personal.studentName} archived successfully.`);
  };

  const archiveSelectedStudents = (reason: string) => {
    if (selectedStudentIds.length === 0) return;
    const now = new Date().toISOString();

    const itemsToArchive = students
      .filter((s) => selectedStudentIds.includes(s.id))
      .map((s) => ({
        ...s,
        status: 'Archived' as const,
        archivedAt: now,
        archivedBy: 'Admin User',
        archiveReason: reason,
        updatedAt: now,
      }));

    setStudents((prev) => prev.filter((s) => !selectedStudentIds.includes(s.id)));
    setArchivedStudents((prev) => [...itemsToArchive, ...prev]);
    showSnackbar(`${itemsToArchive.length} student(s) archived successfully.`);
    setSelectedStudentIds([]);
  };

  const restoreStudents = (studentIds: string[]) => {
    const now = new Date().toISOString();
    const itemsToRestore = archivedStudents
      .filter((s) => studentIds.includes(s.id))
      .map((s) => ({
        ...s,
        status: 'Active' as const,
        archivedAt: undefined,
        archivedBy: undefined,
        archiveReason: undefined,
        updatedAt: now,
      }));

    setArchivedStudents((prev) => prev.filter((s) => !studentIds.includes(s.id)));
    setStudents((prev) => [...itemsToRestore, ...prev]);
    showSnackbar(`${itemsToRestore.length} student(s) restored to Active list successfully.`);
  };

  const bulkUpdateFromRows = (updatedRows: Partial<StudentRecord>[]) => {
    let updatedCount = 0;
    let skippedCount = 0;
    const now = new Date().toISOString();

    setStudents((prev) => {
      const copy = [...prev];
      updatedRows.forEach((row) => {
        const appNo = row.personal?.applicationNumber?.trim();
        const regNo = row.personal?.registerNumber?.trim();

        const matchIndex = copy.findIndex(
          (s) =>
            (regNo && s.personal.registerNumber.toLowerCase() === regNo.toLowerCase()) ||
            (appNo && s.personal.applicationNumber.toLowerCase() === appNo.toLowerCase())
        );

        if (matchIndex !== -1) {
          copy[matchIndex] = {
            ...copy[matchIndex],
            personal: { ...copy[matchIndex].personal, ...row.personal },
            parent: { ...copy[matchIndex].parent, ...row.parent },
            communication: {
              ...copy[matchIndex].communication,
              permanentAddress: {
                ...copy[matchIndex].communication.permanentAddress,
                ...row.communication?.permanentAddress,
              },
            },
            updatedAt: now,
          };
          updatedCount++;
        } else {
          skippedCount++;
        }
      });
      return copy;
    });

    showSnackbar(`Bulk update finished! ${updatedCount} updated, ${skippedCount} skipped.`);
    return { updatedCount, skippedCount };
  };

  return (
    <AdmissionContext.Provider
      value={{
        students,
        archivedStudents,
        selectedStudentIds,
        setSelectedStudentIds,
        activeStep,
        setActiveStep,
        draftStudent,
        setDraftStudent,
        updateDraftSection,
        isEditMode,
        isViewReadOnly,
        editingStudentId,
        viewModalStudent,
        closeViewModal,
        startAddAdmission,
        startEditStudent,
        startViewStudent,
        saveCurrentAdmission,
        archiveSelectedStudents,
        archiveSingleStudent,
        restoreStudents,
        bulkUpdateFromRows,
        snackbar,
        showSnackbar,
        hideSnackbar,
        confirmDialog,
        showConfirm,
        hideConfirm,
        bulkUpdateModalOpen,
        setBulkUpdateModalOpen,
      }}
    >
      {children}
    </AdmissionContext.Provider>
  );
};
