import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Paper,
  CircularProgress,
  Chip,
} from '@mui/material';
import { UploadCloud, FileSpreadsheet, Download, CheckCircle2, AlertCircle } from 'lucide-react';
import * as XLSX from 'xlsx';
import { useAdmission } from '../../context/AdmissionContext';
import { downloadSampleBulkUpdateTemplate } from '../../utils/exportExcel';
import { StudentRecord } from '../../types';

export const BulkUpdateModal: React.FC = () => {
  const { bulkUpdateModalOpen, setBulkUpdateModalOpen, bulkUpdateFromRows } = useAdmission();
  const [file, setFile] = useState<File | null>(null);
  const [parsedRows, setParsedRows] = useState<Partial<StudentRecord>[]>([]);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [summary, setSummary] = useState<{ total: number; valid: number; invalid: number; duplicate: number } | null>(null);

  const handleClose = () => {
    setFile(null);
    setParsedRows([]);
    setSummary(null);
    setIsProcessing(false);
    setBulkUpdateModalOpen(false);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (!selectedFile) return;

    if (!selectedFile.name.match(/\.(xlsx|xls)$/)) {
      alert('Please select a valid Excel file (.xlsx or .xls)');
      return;
    }

    setFile(selectedFile);
    parseExcel(selectedFile);
  };

  const parseExcel = (fileToParse: File) => {
    const reader = new FileReader();
    reader.onload = (evt) => {
      const bstr = evt.target?.result;
      const wb = XLSX.read(bstr, { type: 'binary' });
      const wsname = wb.SheetNames[0];
      const ws = wb.Sheets[wsname];
      const data: any[] = XLSX.utils.sheet_to_json(ws);

      const rows: Partial<StudentRecord>[] = data.map((row) => ({
        personal: {
          applicationNumber: row['Application Number'] || row['ApplicationNo'] || '',
          registerNumber: row['Register Number'] || row['RegisterNo'] || '',
          studentName: row['Student Name'] || row['StudentName'] || '',
          district: row['District'] || 'Puducherry',
          dateOfBirth: row['DOB'] || '2005-01-01',
          age: 21,
          aadhaarNumber: row['Aadhaar'] || '123456789012',
          gender: 'Male',
          nationality: 'Indian',
          caste: 'OC',
        },
        parent: {
          fatherName: row['Father Name'] || '',
          fatherMobile: String(row['Father Mobile'] || ''),
          fatherOccupation: '',
          annualIncome: 0,
        },
        communication: {
          permanentAddress: {
            addressLine: row['Address'] || '',
            pinCode: '605001',
            mobileNumber: String(row['Mobile Number'] || ''),
            email: row['Email'] || '',
          },
          communicationAddress: {
            addressLine: row['Address'] || '',
            pinCode: '605001',
            mobileNumber: String(row['Mobile Number'] || ''),
            email: row['Email'] || '',
          },
          sameAsPermanent: true,
        },
      }));

      const total = rows.length;
      const valid = rows.filter((r) => r.personal?.registerNumber || r.personal?.applicationNumber).length;
      const invalid = total - valid;
      const duplicate = 0;

      setParsedRows(rows);
      setSummary({ total, valid, invalid, duplicate });
    };
    reader.readAsBinaryString(fileToParse);
  };

  const handleProcessExcel = () => {
    if (parsedRows.length === 0) return;
    setIsProcessing(true);
    setTimeout(() => {
      bulkUpdateFromRows(parsedRows);
      setIsProcessing(false);
      handleClose();
    }, 600);
  };

  return (
    <Dialog
      open={bulkUpdateModalOpen}
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: '16px',
          padding: '8px',
        },
      }}
    >
      <DialogTitle sx={{ fontWeight: 700, color: '#0D47A1', fontSize: '1.25rem' }}>
        Bulk Student Update
      </DialogTitle>
      <DialogContent>
        <Typography variant="body2" sx={{ color: '#667085', marginBottom: '20px' }}>
          Upload an Excel file to update existing student records in bulk. Only existing students will be updated based on their Register Number or Application Number.
        </Typography>

        <Button
          variant="outlined"
          startIcon={<Download size={16} />}
          onClick={downloadSampleBulkUpdateTemplate}
          sx={{
            borderColor: '#0D47A1',
            color: '#0D47A1',
            borderRadius: '8px',
            marginBottom: '20px',
            fontWeight: 600,
          }}
        >
          Download Sample Excel Template
        </Button>

        {/* Upload Drop Zone */}
        <Paper
          component="label"
          variant="outlined"
          sx={{
            border: '2px dashed #38BDF8',
            borderRadius: '12px',
            padding: '32px 20px',
            textAlign: 'center',
            backgroundColor: '#F5F8FC',
            cursor: 'pointer',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '12px',
            transition: 'all 200ms',
            '&:hover': { backgroundColor: '#EBF3FE', borderColor: '#0D47A1' },
          }}
        >
          <input
            type="file"
            hidden
            accept=".xlsx, .xls"
            onChange={handleFileChange}
          />
          <UploadCloud size={44} color="#0D47A1" />
          <Typography variant="body1" sx={{ fontWeight: 600, color: '#1A2B49' }}>
            {file ? file.name : 'Drag & Drop Excel File or Click to Browse'}
          </Typography>
          <Typography variant="caption" sx={{ color: '#667085' }}>
            Accepted files: .xlsx, .xls (Max: 20 MB)
          </Typography>
        </Paper>

        {/* Validation Summary */}
        {summary && (
          <Box sx={{ marginTop: '20px', backgroundColor: '#F8FAFC', padding: '16px', borderRadius: '12px', border: '1px solid #E6ECF5' }}>
            <Typography variant="h6" sx={{ fontSize: '0.95rem', fontWeight: 700, color: '#1A2B49', marginBottom: '12px' }}>
              Validation Summary
            </Typography>
            <Box sx={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <Chip label={`Total Records: ${summary.total}`} color="primary" variant="outlined" sx={{ fontWeight: 600 }} />
              <Chip label={`Valid: ${summary.valid}`} color="success" icon={<CheckCircle2 size={14} />} sx={{ fontWeight: 600 }} />
              <Chip label={`Invalid: ${summary.invalid}`} color="error" icon={<AlertCircle size={14} />} sx={{ fontWeight: 600 }} />
              <Chip label={`Duplicate: ${summary.duplicate}`} variant="outlined" sx={{ fontWeight: 600 }} />
            </Box>
          </Box>
        )}
      </DialogContent>

      <DialogActions sx={{ padding: '16px 24px' }}>
        <Button onClick={handleClose} variant="outlined" sx={{ borderColor: '#D8E4F2', color: '#667085', borderRadius: '8px' }}>
          Cancel
        </Button>
        <Button
          onClick={handleProcessExcel}
          variant="contained"
          disabled={!file || parsedRows.length === 0 || isProcessing}
          startIcon={isProcessing ? <CircularProgress size={16} color="inherit" /> : <FileSpreadsheet size={16} />}
          sx={{
            backgroundColor: '#0D47A1',
            borderRadius: '8px',
            fontWeight: 600,
            padding: '8px 20px',
          }}
        >
          {isProcessing ? 'Processing...' : 'Process Excel'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
