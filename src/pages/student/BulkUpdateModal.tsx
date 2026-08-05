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
import {
  downloadSampleBulkUpdateTemplate,
  exportBulkUpdateErrorReport,
} from '../../utils/exportExcel';
import { BulkUpdateRowInput, BulkUpdateResult } from '../../types';

const toDateString = (v: unknown): string | undefined => {
  if (v instanceof Date) {
    if (Number.isNaN(v.getTime())) return undefined;
    const y = v.getFullYear();
    const m = String(v.getMonth() + 1).padStart(2, '0');
    const d = String(v.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
  if (typeof v === 'number' && Number.isFinite(v)) {
    if (v > 0 && v < 60000) {
      const code = XLSX.SSF.parse_date_code(v);
      if (code) {
        return `${code.y}-${String(code.m).padStart(2, '0')}-${String(code.d).padStart(2, '0')}`;
      }
    }
    return String(v);
  }
  if (typeof v === 'string') {
    const s = v.trim();
    return s || undefined;
  }
  return undefined;
};

const cellValue = (row: Record<string, unknown>, keys: string[]): string | undefined => {
  for (const key of keys) {
    const v = row[key];
    if (v === undefined || v === null) continue;
    if (typeof v === 'number' && Number.isFinite(v)) return String(v);
    const s = String(v).trim();
    if (s) return s;
  }
  return undefined;
};

export const BulkUpdateModal: React.FC = () => {
  const { bulkUpdateModalOpen, setBulkUpdateModalOpen, bulkUpdateFromRows } = useAdmission();
  const [file, setFile] = useState<File | null>(null);
  const [parsedRows, setParsedRows] = useState<BulkUpdateRowInput[]>([]);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [result, setResult] = useState<BulkUpdateResult | null>(null);

  const handleClose = () => {
    setFile(null);
    setParsedRows([]);
    setResult(null);
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
      const data: Record<string, unknown>[] = XLSX.utils.sheet_to_json(ws, { defval: '' });

      const rows: BulkUpdateRowInput[] = data.map((row, index) => ({
        rowNumber: index + 2,
        applicationNumber: cellValue(row, ['Application No', 'Application Number', 'ApplicationNo']),
        registerNumber: cellValue(row, ['Register No', 'Register Number', 'RegisterNo']),
        studentName: cellValue(row, ['Student Name', 'StudentName']),
        dateOfBirth: toDateString(row['DOB'] ?? row['Date of Birth'] ?? ''),
        gender: cellValue(row, ['Gender']),
        aadhaarNumber: cellValue(row, ['Aadhaar No', 'Aadhaar Number', 'Aadhaar']),
        district: cellValue(row, ['District']),
        caste: cellValue(row, ['Category']),
        admissionCategory: cellValue(row, ['Admission Category']),
        program: cellValue(row, ['Program']),
        department: cellValue(row, ['Department']),
        batch: cellValue(row, ['Batch']),
        fatherName: cellValue(row, ['Father Name']),
        fatherMobile: cellValue(row, ['Father Mobile', 'Father Mobile Number']),
        mobileNumber: cellValue(row, ['Mobile Number', 'Mobile']),
        email: cellValue(row, ['Email', 'Email ID']),
        grandTotalFee: cellValue(row, ['Grand Total Fee (₹)', 'Grand Total Fee', 'Total Fee']),
        status: cellValue(row, ['Status']),
        archiveReason: cellValue(row, ['Archive Reason']),
      }));

      setParsedRows(rows);
      setResult(null);
    };
    reader.readAsBinaryString(fileToParse);
  };

  const handleProcessExcel = async () => {
    if (parsedRows.length === 0 || isProcessing) return;
    setIsProcessing(true);
    const res = await bulkUpdateFromRows(parsedRows);
    setResult(res);
    setIsProcessing(false);
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

        {/* Processing Summary */}
        {result && (
          <Box sx={{ marginTop: '20px', backgroundColor: '#F8FAFC', padding: '16px', borderRadius: '12px', border: '1px solid #E6ECF5' }}>
            <Typography variant="h6" sx={{ fontSize: '0.95rem', fontWeight: 700, color: '#1A2B49', marginBottom: '12px' }}>
              Processing Summary
            </Typography>
            <Box sx={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <Chip label={`Total Records: ${result.totalRows}`} color="primary" variant="outlined" sx={{ fontWeight: 600 }} />
              <Chip label={`Successfully Updated: ${result.updatedCount}`} color="success" icon={<CheckCircle2 size={14} />} sx={{ fontWeight: 600 }} />
              <Chip label={`Skipped: ${result.skippedCount}`} color="warning" icon={<AlertCircle size={14} />} sx={{ fontWeight: 600 }} />
              <Chip label={`Failed: ${result.failedCount}`} color="error" icon={<AlertCircle size={14} />} sx={{ fontWeight: 600 }} />
            </Box>
            {result.errors.length > 0 && (
              <Button
                variant="outlined"
                startIcon={<Download size={16} />}
                onClick={() => exportBulkUpdateErrorReport(result.errors)}
                sx={{
                  borderColor: '#0D47A1',
                  color: '#0D47A1',
                  borderRadius: '8px',
                  marginTop: '16px',
                  fontWeight: 600,
                }}
              >
                Download Error Report
              </Button>
            )}
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
          disabled={!file || parsedRows.length === 0 || isProcessing || !!result}
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
