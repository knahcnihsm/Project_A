import React, { useRef, useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Chip,
  Alert,
  AlertTitle,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  CircularProgress,
  Tooltip,
} from '@mui/material';
import {
  FileSpreadsheet,
  Upload,
  ShieldCheck,
  CheckCircle2,
  XCircle,
  ChevronDown,
  ListChecks,
  Download,
} from 'lucide-react';
import * as XLSX from 'xlsx';
import {
  bulkUpdateApi,
} from '../../api/client';
import {
  BulkUpdateSchema,
  BulkUpdatePreview,
  BulkUpdateApply,
  BulkUpdateSheet,
  BulkUpdateColumn,
  BulkUpdateTable,
} from '../../api/types';

interface ParsedSheet {
  tableName: string;
  rows: Record<string, string>[];
}

const toRowString = (value: unknown): string => {
  if (value === null || value === undefined) return '';
  if (typeof value === 'number') {
    return Number.isInteger(value) ? String(value) : String(value);
  }
  return String(value);
};

const tableColumns = (table: BulkUpdateTable): string[] => table.columns.map((c) => c.name);

const SAMPLE_DATA: Record<string, Record<string, string>[]> = {
  student_details: [
    {
      application_no: 'RGCET/2026/2001',
      register_no: '26BTECH001',
      student_name: 'Aarav Sharma',
      date_of_birth: '2008-06-10',
      aadhaar_no: '123412341200',
      gender: 'Male',
      district: 'Karaikal',
      nationality: 'Indian',
      caste: 'OTHERS',
      mobile_number: '9840112233',
      email_id: 'aarav.sharma@example.com',
    },
  ],
  parent_details: [
    {
      application_no: 'RGCET/2026/2001',
      father_name: 'Father 1',
      father_mobile_no: '9876543001',
      father_occupation: 'Farmer',
      annual_income: '400000',
    },
  ],
  admission: [
    {
      application_no: 'RGCET/2026/2001',
      category_id: 'Management',
      program_id: 'First Year B.Tech',
      department_id: 'Computer Science & Engineering (CSE)',
      batch: '2026-2030',
      date_of_admission: '2026-08-01',
    },
  ],
  address: [
    {
      application_no: 'RGCET/2026/2001',
      address_type: 'PERMANENT',
      address_line: 'No.1, Gandhi Street, Puducherry',
      pincode: '605002',
      phone: '',
      mobile: '9876543001',
      email: 'student1@mail.com',
      same_as_permanent: '',
    },
    {
      application_no: 'RGCET/2026/2001',
      address_type: 'COMMUNICATION',
      address_line: 'No.1, Gandhi Street, Puducherry',
      pincode: '605002',
      phone: '',
      mobile: '9876543001',
      email: 'student1@mail.com',
      same_as_permanent: '',
    },
  ],
  qualifying_examination: [
    {
      application_no: 'RGCET/2026/2001',
      institution_name: 'Govt Hr Sec School',
      institution_place: 'Puducherry',
      exam_passed: 'HSC',
      month_year_of_passing: 'May 2026',
      sslc_registration_no: 'SSLC001',
      sslc_percentage: '92',
      hsc_registration_no: 'HSC001',
      hsc_percentage: '89',
    },
  ],
  student_fee: [
    {
      application_no: 'RGCET/2026/2001',
      cut_off_mark: '284',
      merit_percent: '94.67',
      original_tuition_fee: '100000',
      scholarship_amount: '20000',
      tuition_fee_per_year: '80000',
      course_duration_years: '4',
      total_tuition_fee: '320000',
      bus_required: 'FALSE',
      route_id: '',
      bus_stop_id: '',
      bus_fee: '0',
      hostel_required: 'FALSE',
      hostel_id: '',
      hostel_fee: '0',
      paid_amount: '',
      pending_amount: '',
    },
  ],
  student_certificate: [
    {
      application_no: 'RGCET/2026/2001',
      certificate_id: 'Provisional Allotment Order',
      is_submitted: 'TRUE',
      file_path: '',
    },
  ],
};

export const BulkUpdatePage: React.FC = () => {
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [schema, setSchema] = useState<BulkUpdateSchema | null>(null);
  const [schemaError, setSchemaError] = useState<string>('');

  const [parsedSheets, setParsedSheets] = useState<ParsedSheet[]>([]);
  const [fileName, setFileName] = useState<string>('');

  const [preview, setPreview] = useState<BulkUpdatePreview | null>(null);
  const [applyResult, setApplyResult] = useState<BulkUpdateApply | null>(null);

  const [loadingSchema, setLoadingSchema] = useState(false);
  const [validating, setValidating] = useState(false);
  const [applying, setApplying] = useState(false);
  const [error, setError] = useState<string>('');

  const loadSchema = async () => {
    setLoadingSchema(true);
    setSchemaError('');
    try {
      setSchema(await bulkUpdateApi.schema());
    } catch (e) {
      setSchemaError(e instanceof Error ? e.message : 'Failed to load schema');
    } finally {
      setLoadingSchema(false);
    }
  };

  React.useEffect(() => {
    loadSchema();
  }, []);

  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setError('');
    setPreview(null);
    setApplyResult(null);
    try {
      const buffer = await file.arrayBuffer();
      const workbook = XLSX.read(buffer, { type: 'array' });
      const sheets: ParsedSheet[] = workbook.SheetNames.map((name) => {
        const worksheet = workbook.Sheets[name];
        const rawRows = XLSX.utils.sheet_to_json<Record<string, unknown>>(worksheet, {
          defval: '',
          raw: false,
        });
        const rows = rawRows.map((raw) => {
          const normalized: Record<string, string> = {};
          Object.entries(raw).forEach(([key, value]) => {
            normalized[key.trim()] = toRowString(value).trim();
          });
          return normalized;
        });
        return { tableName: name.trim(), rows };
      });
      setParsedSheets(sheets);
      setFileName(file.name);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to parse the Excel file');
    }
    event.target.value = '';
  };

  const handleValidate = async () => {
    if (parsedSheets.length === 0) return;
    setValidating(true);
    setError('');
    setPreview(null);
    setApplyResult(null);
    try {
      const payload: { sheets: BulkUpdateSheet[] } = {
        sheets: parsedSheets.map((s) => ({ tableName: s.tableName, rows: s.rows })),
      };
      setPreview(await bulkUpdateApi.validate(payload));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Validation failed');
    } finally {
      setValidating(false);
    }
  };

  const handleApply = async () => {
    if (!preview || parsedSheets.length === 0) return;
    setApplying(true);
    setError('');
    try {
      const payload: { sheets: BulkUpdateSheet[] } = {
        sheets: parsedSheets.map((s) => ({ tableName: s.tableName, rows: s.rows })),
      };
      setApplyResult(await bulkUpdateApi.apply(payload));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Apply failed');
    } finally {
      setApplying(false);
    }
  };

  const handleDownloadTemplate = () => {
    if (!schema) return;
    const workbook = XLSX.utils.book_new();
    schema.tables.forEach((table) => {
      const worksheet = XLSX.utils.aoa_to_sheet([tableColumns(table)]);
      XLSX.utils.book_append_sheet(workbook, worksheet, table.tableName.slice(0, 31));
    });
    XLSX.writeFile(workbook, 'bulk_update_template.xlsx');
  };

  const handleDownloadSample = () => {
    if (!schema) return;
    const workbook = XLSX.utils.book_new();
    schema.tables.forEach((table) => {
      const header = tableColumns(table);
      const rows = SAMPLE_DATA[table.tableName] || [];
      const sheetRows = [header, ...rows.map((row) => header.map((col) => row[col] ?? ''))];
      const worksheet = XLSX.utils.aoa_to_sheet(sheetRows);
      XLSX.utils.book_append_sheet(workbook, worksheet, table.tableName.slice(0, 31));
    });
    XLSX.writeFile(workbook, 'bulk_update_sample.xlsx');
  };

  const handleDownloadErrorReport = () => {
    const records = (preview?.records || [])
      .filter((r) => !r.valid)
      .map((r) => ({
        'Application No': r.applicationNo,
        'Student Name': r.studentName,
        Status: 'INVALID',
        Errors: r.errors.join('; '),
      }));
    if (records.length === 0) {
      (applyResult?.results || [])
        .filter((r) => r.status === 'FAILED')
        .forEach((r) => {
          records.push({
            'Application No': r.applicationNo,
            'Student Name': r.studentName,
            Status: r.status,
            Errors: r.errors.join('; '),
          });
        });
    }
    if (records.length === 0) return;
    const worksheet = XLSX.utils.json_to_sheet(records);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Error Report');
    XLSX.writeFile(workbook, 'bulk_update_error_report.xlsx');
  };

  const totalParsedRows = parsedSheets.reduce((sum, s) => sum + s.rows.length, 0);
  const validPreviewCount = preview?.records.filter((r) => r.valid).length ?? 0;

  const columnTypeLabel = (col: BulkUpdateColumn) => {
    if (col.type === 'REFERENCE') return `Reference (${col.fkReference})`;
    if (col.type === 'ENUM') return 'Dropdown';
    if (col.type === 'NUMBER') return 'Number';
    if (col.type === 'DATE') return 'Date';
    if (col.type === 'BOOLEAN') return 'Yes/No';
    return 'Text';
  };

  const renderSummaryChip = (
    label: string,
    value: number,
    color: string,
    bg: string
  ) => (
    <Box sx={{ padding: '12px 16px', borderRadius: '10px', backgroundColor: bg, textAlign: 'center', minWidth: '110px' }}>
      <Typography sx={{ fontWeight: 800, fontSize: '1.4rem', color, lineHeight: 1.1 }}>
        {value}
      </Typography>
      <Typography sx={{ fontSize: '0.72rem', fontWeight: 600, color: '#667085', letterSpacing: '0.04em' }}>
        {label}
      </Typography>
    </Box>
  );

  return (
    <Box>
      <Box sx={{ marginBottom: '24px' }}>
        <Typography variant="h4" sx={{ fontWeight: 800, color: '#0D47A1', fontSize: '1.75rem' }}>
          Bulk Student Update
        </Typography>
        <Typography variant="body1" sx={{ color: '#667085', fontSize: '0.95rem' }}>
          Upload an Excel workbook and apply field-level changes to existing admission records.
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ marginBottom: '16px' }}>
          <AlertTitle>Operation Failed</AlertTitle>
          {error}
        </Alert>
      )}

      {/* Schema Reference */}
      <Card elevation={0} sx={{ border: '1px solid #E6ECF5', borderRadius: '16px', backgroundColor: '#FFFFFF', marginBottom: '20px' }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
            <Box sx={{ width: 40, height: 40, borderRadius: '10px', backgroundColor: '#E0F2FE', color: '#0284C7', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <ListChecks size={20} />
            </Box>
            <Box sx={{ flex: 1 }}>
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', fontSize: '1rem' }}>
                Updatable Tables & Columns
              </Typography>
            </Box>
            <Button
              size="small"
              startIcon={<Download size={16} />}
              onClick={handleDownloadSample}
              disabled={!schema}
              sx={{ color: '#16A34A', fontWeight: 600 }}
            >
              Download Sample
            </Button>
            <Button
              size="small"
              startIcon={<Download size={16} />}
              onClick={handleDownloadTemplate}
              disabled={!schema}
              sx={{ color: '#0284C7', fontWeight: 600 }}
            >
              Download Template
            </Button>
          </Box>

          {loadingSchema && <CircularProgress size={20} sx={{ color: '#0284C7' }} />}
          {schemaError && (
            <Alert severity="warning" sx={{ fontSize: '0.85rem' }}>
              {schemaError} — the bulk update API may be offline.
            </Alert>
          )}

          {schema &&
            schema.tables.map((table) => (
              <Accordion key={table.tableName} disableGutters sx={{ boxShadow: 'none', border: '1px solid #E6ECF5', borderRadius: '8px !important', marginBottom: '8px', '&:before': { display: 'none' } }}>
                <AccordionSummary expandIcon={<ChevronDown size={16} />} sx={{ minHeight: '40px', '& .MuiAccordionSummary-content': { margin: 0 } }}>
                  <Typography sx={{ fontWeight: 700, fontSize: '0.88rem', color: '#1A2B49', textTransform: 'uppercase', letterSpacing: '0.03em' }}>
                    {table.tableName}
                  </Typography>
                </AccordionSummary>
                <AccordionDetails sx={{ paddingTop: 0 }}>
                  <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid #EEF2F7', borderRadius: '8px' }}>
                    <Table size="small">
                      <TableHead>
                        <TableRow sx={{ backgroundColor: '#F8FAFC' }}>
                          <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Column</TableCell>
                          <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Type</TableCell>
                          <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Required</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {table.columns.map((col) => (
                          <TableRow key={col.name} sx={{ '&:last-child td': { borderBottom: 0 } }}>
                            <TableCell sx={{ fontSize: '0.8rem', fontFamily: 'monospace' }}>
                              {col.isKey && (
                                <Tooltip title="Lookup key">
                                  <Chip label="KEY" size="small" sx={{ height: 18, fontSize: '0.6rem', marginRight: '6px', backgroundColor: '#FEF3C7', color: '#D97706' }} />
                                </Tooltip>
                              )}
                              {col.name}
                            </TableCell>
                            <TableCell sx={{ fontSize: '0.8rem', color: '#475569' }}>
                              {columnTypeLabel(col)}
                            </TableCell>
                            <TableCell sx={{ fontSize: '0.8rem', color: '#475569' }}>
                              {col.required ? 'Yes' : 'No'}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </AccordionDetails>
              </Accordion>
            ))}
        </CardContent>
      </Card>

      {/* Upload */}
      <Card elevation={0} sx={{ border: '1px solid #E6ECF5', borderRadius: '16px', backgroundColor: '#FFFFFF', marginBottom: '20px' }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
            <Box sx={{ width: 40, height: 40, borderRadius: '10px', backgroundColor: '#DCFCE7', color: '#16A34A', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <FileSpreadsheet size={20} />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', fontSize: '1rem' }}>
                1. Upload Excel Workbook
              </Typography>
              <Typography variant="body2" sx={{ color: '#667085' }}>
                Each sheet name must match a table name; the first row is the header.
              </Typography>
            </Box>
          </Box>

          <input
            ref={fileInputRef}
            type="file"
            accept=".xlsx,.xls,.csv"
            style={{ display: 'none' }}
            onChange={handleFileChange}
          />

          <Box sx={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center' }}>
            <Button
              variant="contained"
              startIcon={<Upload size={18} />}
              onClick={() => fileInputRef.current?.click()}
              sx={{ backgroundColor: '#16A34A', borderRadius: '8px', fontWeight: 600 }}
            >
              Choose Excel File
            </Button>
            {fileName && (
              <Chip icon={<FileSpreadsheet size={14} />} label={fileName} variant="outlined" sx={{ color: '#1A2B49' }} />
            )}
            {parsedSheets.length > 0 && (
              <Typography sx={{ fontSize: '0.85rem', color: '#475569' }}>
                {parsedSheets.length} sheet(s), {totalParsedRows} row(s)
              </Typography>
            )}
          </Box>
        </CardContent>
      </Card>

      {/* Validate */}
      <Card elevation={0} sx={{ border: '1px solid #E6ECF5', borderRadius: '16px', backgroundColor: '#FFFFFF', marginBottom: '20px' }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
            <Box sx={{ width: 40, height: 40, borderRadius: '10px', backgroundColor: '#E0F2FE', color: '#0284C7', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <ShieldCheck size={20} />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', fontSize: '1rem' }}>
                2. Validate & Preview Changes
              </Typography>
              <Typography variant="body2" sx={{ color: '#667085' }}>
                No data is modified — the server returns a preview of every change.
              </Typography>
            </Box>
          </Box>

          <Button
            variant="contained"
            startIcon={validating ? <CircularProgress size={16} color="inherit" /> : <ShieldCheck size={18} />}
            onClick={handleValidate}
            disabled={validating || parsedSheets.length === 0}
            sx={{ backgroundColor: '#0284C7', borderRadius: '8px', fontWeight: 600 }}
          >
            {validating ? 'Validating…' : 'Validate File'}
          </Button>
        </CardContent>
      </Card>

      {/* Preview */}
      {preview && (
        <Card elevation={0} sx={{ border: '1px solid #E6ECF5', borderRadius: '16px', backgroundColor: '#FFFFFF', marginBottom: '20px' }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
              <Box sx={{ width: 40, height: 40, borderRadius: '10px', backgroundColor: '#FEF3C7', color: '#D97706', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <CheckCircle2 size={20} />
              </Box>
              <Box sx={{ flex: 1 }}>
                <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', fontSize: '1rem' }}>
                  3. Preview Result
                </Typography>
              </Box>
            </Box>

            <Box sx={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginBottom: '16px' }}>
              {renderSummaryChip('TOTAL', preview.summary.totalRecords, '#1A2B49', '#F1F5F9')}
              {renderSummaryChip('VALID', preview.summary.validRecords, '#16A34A', '#DCFCE7')}
              {renderSummaryChip('INVALID', preview.summary.invalidRecords, '#DC2626', '#FEE2E2')}
              {renderSummaryChip('WILL CHANGE', preview.summary.changedRecords, '#D97706', '#FEF3C7')}
              {renderSummaryChip('UNCHANGED', preview.summary.unchangedRecords, '#475569', '#E2E8F0')}
            </Box>

            {preview.summary.invalidRecords > 0 && (
              <Button
                size="small"
                startIcon={<Download size={16} />}
                onClick={handleDownloadErrorReport}
                sx={{ color: '#DC2626', fontWeight: 600, marginBottom: '12px' }}
              >
                Download Error Report
              </Button>
            )}

            <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid #EEF2F7', borderRadius: '8px' }}>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ backgroundColor: '#F8FAFC' }}>
                    <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>#</TableCell>
                    <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Application No</TableCell>
                    <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Student</TableCell>
                    <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Status</TableCell>
                    <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Changes</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {preview.records.map((record, index) => (
                    <TableRow key={`${record.applicationNo}-${index}`} sx={{ '&:last-child td': { borderBottom: 0 } }}>
                      <TableCell sx={{ fontSize: '0.8rem', color: '#94A3B8' }}>{index + 1}</TableCell>
                      <TableCell sx={{ fontSize: '0.8rem', fontFamily: 'monospace' }}>{record.applicationNo}</TableCell>
                      <TableCell sx={{ fontSize: '0.8rem' }}>{record.studentName}</TableCell>
                      <TableCell>
                        {record.valid ? (
                          <Chip icon={<CheckCircle2 size={13} />} label="Valid" size="small" sx={{ backgroundColor: '#DCFCE7', color: '#16A34A', fontSize: '0.7rem', fontWeight: 700 }} />
                        ) : (
                          <Chip icon={<XCircle size={13} />} label="Invalid" size="small" sx={{ backgroundColor: '#FEE2E2', color: '#DC2626', fontSize: '0.7rem', fontWeight: 700 }} />
                        )}
                      </TableCell>
                      <TableCell sx={{ maxWidth: '420px' }}>
                        {record.errors.length > 0 && (
                          <Box sx={{ color: '#DC2626', fontSize: '0.75rem', marginBottom: record.changes.length ? '4px' : 0 }}>
                            {record.errors.map((e, i) => (
                              <Box key={i}>• {e}</Box>
                            ))}
                          </Box>
                        )}
                        {record.changes.map((change, i) => (
                          <Box key={i} sx={{ fontSize: '0.75rem', color: '#475569', display: 'flex', gap: '4px', alignItems: 'baseline' }}>
                            <Chip label={change.tableName} size="small" sx={{ height: 18, fontSize: '0.62rem', backgroundColor: '#EFF6FF', color: '#1D4ED8' }} />
                            <span style={{ fontFamily: 'monospace' }}>{change.fieldName}:</span>
                            <s style={{ color: '#DC2626' }}>{change.oldValue || '(empty)'}</s>
                            <span style={{ color: '#94A3B8' }}>→</span>
                            <span style={{ color: '#16A34A', fontWeight: 600 }}>{change.newValue || '(empty)'}</span>
                          </Box>
                        ))}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      {/* Apply */}
      {preview && (
        <Card elevation={0} sx={{ border: '1px solid #E6ECF5', borderRadius: '16px', backgroundColor: '#FFFFFF' }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
              <Box sx={{ width: 40, height: 40, borderRadius: '10px', backgroundColor: '#FEE2E2', color: '#DC2626', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Upload size={20} />
              </Box>
              <Box sx={{ flex: 1 }}>
                <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', fontSize: '1rem' }}>
                  4. Apply Changes
                </Typography>
                <Typography variant="body2" sx={{ color: '#667085' }}>
                  {validPreviewCount} valid record(s) will be updated. Invalid records are skipped.
                </Typography>
              </Box>
            </Box>

            <Button
              variant="contained"
              startIcon={applying ? <CircularProgress size={16} color="inherit" /> : <CheckCircle2 size={18} />}
              onClick={handleApply}
              disabled={applying || validPreviewCount === 0}
              sx={{ backgroundColor: '#DC2626', borderRadius: '8px', fontWeight: 600 }}
            >
              {applying ? 'Applying…' : `Apply ${validPreviewCount} Record(s)`}
            </Button>

            {applyResult && (
              <Box sx={{ marginTop: '20px' }}>
                <Box sx={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginBottom: '16px' }}>
                  {renderSummaryChip('TOTAL', applyResult.summary.totalRecords, '#1A2B49', '#F1F5F9')}
                  {renderSummaryChip('UPDATED', applyResult.summary.updatedRecords, '#16A34A', '#DCFCE7')}
                  {renderSummaryChip('SKIPPED', applyResult.summary.skippedRecords, '#D97706', '#FEF3C7')}
                  {renderSummaryChip('FAILED', applyResult.summary.failedRecords, '#DC2626', '#FEE2E2')}
                </Box>
                {applyResult.summary.failedRecords > 0 && (
                  <Button
                    size="small"
                    startIcon={<Download size={16} />}
                    onClick={handleDownloadErrorReport}
                    sx={{ color: '#DC2626', fontWeight: 600, marginBottom: '12px' }}
                  >
                    Download Error Report
                  </Button>
                )}
                <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid #EEF2F7', borderRadius: '8px' }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ backgroundColor: '#F8FAFC' }}>
                        <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Application No</TableCell>
                        <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Student</TableCell>
                        <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Status</TableCell>
                        <TableCell sx={{ fontWeight: 700, fontSize: '0.75rem' }}>Errors</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {applyResult.results.map((result, index) => (
                        <TableRow key={`${result.applicationNo}-${index}`} sx={{ '&:last-child td': { borderBottom: 0 } }}>
                          <TableCell sx={{ fontSize: '0.8rem', fontFamily: 'monospace' }}>{result.applicationNo}</TableCell>
                          <TableCell sx={{ fontSize: '0.8rem' }}>{result.studentName}</TableCell>
                          <TableCell>
                            <Chip
                              label={result.status}
                              size="small"
                              sx={{
                                fontSize: '0.7rem',
                                fontWeight: 700,
                                backgroundColor: result.status === 'UPDATED' ? '#DCFCE7' : '#FEE2E2',
                                color: result.status === 'UPDATED' ? '#16A34A' : '#DC2626',
                              }}
                            />
                          </TableCell>
                          <TableCell sx={{ fontSize: '0.75rem', color: '#DC2626' }}>
                            {result.errors.join('; ')}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Box>
            )}
          </CardContent>
        </Card>
      )}
    </Box>
  );
};
