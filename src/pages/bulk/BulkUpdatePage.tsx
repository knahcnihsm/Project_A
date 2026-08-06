import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Stepper,
  Step,
  StepLabel,
  Chip,
  CircularProgress,
  Alert,
  Divider,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  useTheme,
} from '@mui/material';
import {
  Download,
  UploadCloud,
  FileSpreadsheet,
  CheckCircle2,
  AlertCircle,
  AlertTriangle,
  ArrowRight,
  ArrowLeft,
  RefreshCcw,
  FileText,
  ClipboardList,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAdmission } from '../../context/AdmissionContext';
import {
  downloadBulkTemplate,
  parseBulkWorkbookFile,
  exportBulkIssuesReport,
  exportBulkPreviewReport,
  exportBulkCommitReport,
  BULK_SHEET_NAMES,
  DATA_SHEETS,
  BulkWorkbookParseResult,
} from '../../utils/bulkWorkbook';
import { bulkApi } from '../../api/client';
import {
  BulkValidationResponse,
  BulkCommitResponse,
  BulkIssueDto,
} from '../../api/types';

const STEPS = [
  'Introduction',
  'Template',
  'Upload',
  'Structure',
  'Validate',
  'Preview',
  'Confirm',
  'Report',
];

interface StepCardProps {
  isDark: boolean;
  children: React.ReactNode;
}

const StepCard: React.FC<StepCardProps> = ({ isDark, children }) => (
  <Paper
    elevation={0}
    sx={{
      borderRadius: '12px',
      padding: '24px',
      backgroundColor: isDark ? '#161B22' : '#FFFFFF',
      border: isDark ? '1px solid #2D333B' : '1px solid #E6ECF5',
    }}
  >
    {children}
  </Paper>
);

const severityColor = (severity: BulkIssueDto['severity']) =>
  severity === 'ERROR' ? 'error' : 'warning';

export const BulkUpdatePage: React.FC = () => {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const navigate = useNavigate();
  const { showSnackbar, showConfirm, refreshStudents } = useAdmission();

  const [activeStep, setActiveStep] = useState<number>(0);
  const [file, setFile] = useState<File | null>(null);
  const [parseResult, setParseResult] = useState<BulkWorkbookParseResult | null>(null);
  const [validation, setValidation] = useState<BulkValidationResponse | null>(null);
  const [commitResponse, setCommitResponse] = useState<BulkCommitResponse | null>(null);
  const [isParsing, setIsParsing] = useState<boolean>(false);
  const [isValidating, setIsValidating] = useState<boolean>(false);
  const [isCommitting, setIsCommitting] = useState<boolean>(false);
  const [previewPage, setPreviewPage] = useState<number>(0);

  const PREVIEW_PAGE_SIZE = 20;

  const go = (step: number) => {
    setActiveStep(step);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleFileSelect = async (selectedFile: File | null) => {
    if (!selectedFile) return;
    if (!selectedFile.name.match(/\.(xlsx|xls)$/i)) {
      showSnackbar('Please select a valid Excel file (.xlsx or .xls)', 'error');
      return;
    }
    setIsParsing(true);
    try {
      const result = await parseBulkWorkbookFile(selectedFile);
      setFile(selectedFile);
      setParseResult(result);
      setValidation(null);
      setCommitResponse(null);
      setPreviewPage(0);
      go(3);
    } catch {
      showSnackbar('Could not read this workbook. Please use the official template.', 'error');
    } finally {
      setIsParsing(false);
    }
  };

  const handleValidate = async () => {
    if (!parseResult || isValidating) return;
    setIsValidating(true);
    try {
      const response = await bulkApi.validate(parseResult.workbook);
      setValidation(response);
      go(4);
    } catch (e) {
      showSnackbar(e instanceof Error ? e.message : 'Validation failed. Please try again.', 'error');
    } finally {
      setIsValidating(false);
    }
  };

  const handleCommit = async () => {
    if (!parseResult || isCommitting) return;
    setIsCommitting(true);
    try {
      const response = await bulkApi.commit(parseResult.workbook);
      setCommitResponse(response);
      if (response.status === 'SUCCESS') {
        await refreshStudents();
        showSnackbar(
          `Bulk update completed: ${response.updatedStudents} student(s) updated, ${response.failedRows} failed.`,
          'success'
        );
      } else {
        showSnackbar('Bulk update failed. No changes were applied.', 'error');
      }
      go(7);
    } catch (e) {
      showSnackbar(e instanceof Error ? e.message : 'Commit failed. Please try again.', 'error');
    } finally {
      setIsCommitting(false);
    }
  };

  const confirmCommit = () => {
    if (!validation) return;
    showConfirm(
      'Confirm Bulk Update',
      `This will update ${validation.matchedStudents} student record(s) across ${validation.totalRows} row(s) in a single transaction. Any row with a validation error blocks the entire update. Continue?`,
      handleCommit,
      'Commit Updates'
    );
  };

  const handleReset = () => {
    setFile(null);
    setParseResult(null);
    setValidation(null);
    setCommitResponse(null);
    setPreviewPage(0);
    go(0);
  };

  const errorIssues = validation?.issues.filter((i) => i.severity === 'ERROR') ?? [];
  const warningIssues = validation?.issues.filter((i) => i.severity === 'WARNING') ?? [];

  const totalChanges = validation?.preview.reduce((sum, p) => sum + p.changes.length, 0) ?? 0;
  const visiblePreview = validation?.preview.slice(
    0,
    (previewPage + 1) * PREVIEW_PAGE_SIZE
  );

  const canNext = () => {
    switch (activeStep) {
      case 0:
        return true;
      case 1:
        return true;
      case 2:
        return Boolean(file && parseResult);
      case 3:
        return Boolean(parseResult);
      case 4:
        return Boolean(validation && validation.valid);
      case 5:
        return Boolean(validation);
      case 6:
        return Boolean(validation && validation.valid);
      default:
        return false;
    }
  };

  const handleNext = () => {
    if (!canNext()) return;
    if (activeStep === 4 && validation && !validation.valid) return;
    if (activeStep === 6) {
      confirmCommit();
      return;
    }
    go(activeStep + 1);
  };

  const statusChipColor = (status: string) =>
    status === 'SUCCESS' ? 'success' : 'error';

  return (
    <Box sx={{ maxWidth: '1100px', margin: '0 auto' }}>
      {/* Page header */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' }}>
        <Box
          sx={{
            width: '44px',
            height: '44px',
            borderRadius: '10px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: '#0D47A1',
            color: '#FFFFFF',
          }}
        >
          <ClipboardList size={22} />
        </Box>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700, color: '#1A2B49', fontSize: '1.35rem' }}>
            Bulk Student Details Update
          </Typography>
          <Typography variant="body2" sx={{ color: '#667085' }}>
            Update many student records at once using a structured 10-sheet Excel workbook.
          </Typography>
        </Box>
      </Box>

      {/* Stepper */}
      <Stepper activeStep={activeStep} alternativeLabel sx={{ marginBottom: '28px' }}>
        {STEPS.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      {activeStep === 0 && (
        <StepCard isDark={isDark}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', marginBottom: '12px' }}>
            How it works
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {[
              { n: 1, t: 'Download the official template', d: 'A workbook with 10 structured sheets covering every student detail section.' },
              { n: 2, t: 'Fill in only what needs to change', d: 'Each sheet handles one section (personal, parent, addresses, academic, qualifying exam, HSC marks, diploma, PG). Blank cells mean NO CHANGE.' },
              { n: 3, t: 'Upload and validate', d: 'The system matches rows by Application No or Register No and checks every value. Hard errors block the update; warnings never do.' },
              { n: 4, t: 'Review the changes', d: 'See exactly which fields will change for each matched student before anything is written.' },
              { n: 5, t: 'Confirm', d: 'All rows commit in a single transaction - either every valid row is applied or nothing is.' },
              { n: 6, t: 'Get a report', d: 'A downloadable report summarises updated, no-change, skipped and failed rows, and writes an audit log entry.' },
            ].map((item) => (
              <Box key={item.n} sx={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
                <Box
                  sx={{
                    width: '28px',
                    height: '28px',
                    borderRadius: '50%',
                    backgroundColor: '#0D47A1',
                    color: '#FFFFFF',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '14px',
                    fontWeight: 700,
                    flexShrink: 0,
                  }}
                >
                  {item.n}
                </Box>
                <Box>
                  <Typography sx={{ fontWeight: 600, color: '#1A2B49', fontSize: '14px' }}>
                    {item.t}
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#667085' }}>
                    {item.d}
                  </Typography>
                </Box>
              </Box>
            ))}
          </Box>
        </StepCard>
      )}

      {activeStep === 1 && (
        <StepCard isDark={isDark}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', marginBottom: '6px' }}>
            Download the Template
          </Typography>
          <Typography variant="body2" sx={{ color: '#667085', marginBottom: '20px' }}>
            Use the blank template to fill in your own data, or download the sample to see the exact format.
          </Typography>
          <Box sx={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <Button
              variant="contained"
              startIcon={<Download size={16} />}
              onClick={() => downloadBulkTemplate(false)}
              sx={{ backgroundColor: '#0D47A1', borderRadius: '8px', fontWeight: 600, padding: '10px 18px' }}
            >
              Download Blank Template
            </Button>
            <Button
              variant="outlined"
              startIcon={<FileSpreadsheet size={16} />}
              onClick={() => downloadBulkTemplate(true)}
              sx={{ borderColor: '#0D47A1', color: '#0D47A1', borderRadius: '8px', fontWeight: 600, padding: '10px 18px' }}
            >
              Download Sample Template
            </Button>
          </Box>

          <Divider sx={{ margin: '24px 0' }} />

          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#1A2B49', marginBottom: '12px' }}>
            The 10 sheets
          </Typography>
          <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '8px' }}>
            {DATA_SHEETS.map((name, idx) => (
              <Box
                key={name}
                sx={{
                  display: 'flex',
                  gap: '10px',
                  alignItems: 'center',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  backgroundColor: isDark ? '#1C2128' : '#F5F8FC',
                  border: isDark ? '1px solid #2D333B' : '1px solid #E6ECF5',
                }}
              >
                <Chip label={idx + 1} size="small" sx={{ backgroundColor: '#0D47A1', color: '#FFFFFF', fontWeight: 700 }} />
                <Typography variant="body2" sx={{ color: '#1A2B49', fontWeight: 500 }}>
                  {name}
                </Typography>
              </Box>
            ))}
          </Box>
          <Typography variant="caption" sx={{ color: '#667085', display: 'block', marginTop: '10px' }}>
            Sheet 10 (Instructions) is documentation only and is never parsed.
          </Typography>
        </StepCard>
      )}

      {activeStep === 2 && (
        <StepCard isDark={isDark}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', marginBottom: '6px' }}>
            Upload your workbook
          </Typography>
          <Typography variant="body2" sx={{ color: '#667085', marginBottom: '20px' }}>
            Select the filled-in Excel file (.xlsx or .xls) that follows the official template.
          </Typography>

          <Paper
            component="label"
            variant="outlined"
            sx={{
              border: '2px dashed #38BDF8',
              borderRadius: '12px',
              padding: '40px 20px',
              textAlign: 'center',
              backgroundColor: isDark ? '#0D1117' : '#F5F8FC',
              cursor: 'pointer',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '12px',
              transition: 'all 200ms',
              '&:hover': { backgroundColor: isDark ? '#11161D' : '#EBF3FE', borderColor: '#0D47A1' },
            }}
          >
            <input
              type="file"
              hidden
              accept=".xlsx, .xls"
              onChange={(e) => {
                handleFileSelect(e.target.files?.[0] ?? null);
                e.target.value = '';
              }}
            />
            {isParsing ? (
              <CircularProgress size={40} sx={{ color: '#0D47A1' }} />
            ) : (
              <UploadCloud size={44} color="#0D47A1" />
            )}
            <Typography variant="body1" sx={{ fontWeight: 600, color: '#1A2B49' }}>
              {file ? file.name : 'Drag & Drop Excel File or Click to Browse'}
            </Typography>
            <Typography variant="caption" sx={{ color: '#667085' }}>
              Accepted files: .xlsx, .xls
            </Typography>
          </Paper>
        </StepCard>
      )}

      {activeStep === 3 && parseResult && (
        <StepCard isDark={isDark}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49' }}>
              Workbook structure
            </Typography>
            <Chip label={parseResult.fileName} icon={<FileText size={14} />} sx={{ fontWeight: 600 }} />
          </Box>
          <Box sx={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '16px' }}>
            <Chip label={`Data Rows: ${parseResult.totalRows}`} color="primary" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip
              label={`Sheets Found: ${parseResult.sheetsFound.length}/9`}
              color={parseResult.sheetsFound.length === 9 ? 'success' : 'warning'}
              variant="outlined"
              sx={{ fontWeight: 600 }}
            />
            {parseResult.sheetsMissing.length > 0 && (
              <Chip
                label={`Missing Sheets: ${parseResult.sheetsMissing.length}`}
                color="warning"
                icon={<AlertTriangle size={14} />}
                variant="outlined"
                sx={{ fontWeight: 600 }}
              />
            )}
          </Box>

          <Table sx={{ minWidth: '500px' }}>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>#</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Sheet</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Data Rows</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {parseResult.sheetStats.map((stat) => (
                <TableRow key={stat.name}>
                  <TableCell>{BULK_SHEET_NAMES.indexOf(stat.name) + 1}</TableCell>
                  <TableCell sx={{ color: '#1A2B49', fontWeight: 500 }}>{stat.name}</TableCell>
                  <TableCell>{stat.dataRows}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={stat.found ? (stat.dataRows > 0 ? 'Rows' : 'Header Only') : 'Missing'}
                      color={stat.found ? 'success' : 'warning'}
                      icon={stat.found ? <CheckCircle2 size={13} /> : <AlertTriangle size={13} />}
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          {parseResult.sheetsMissing.length > 0 && (
            <Alert severity="info" sx={{ marginTop: '16px', borderRadius: '8px' }}>
              Missing sheets are treated as "no change" for that section. You can still proceed if the sheets you need are present.
            </Alert>
          )}
        </StepCard>
      )}

      {activeStep === 4 && validation && (
        <StepCard isDark={isDark}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', marginBottom: '6px' }}>
            Validation result
          </Typography>
          <Typography variant="body2" sx={{ color: '#667085', marginBottom: '16px' }}>
            Every row was checked against the master data and format rules. Errors block the commit, warnings never do.
          </Typography>

          <Box sx={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '16px' }}>
            <Chip label={`Total Rows: ${validation.totalRows}`} color="primary" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Matched Students: ${validation.matchedStudents}`} color="success" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Unmatched: ${validation.unmatchedRows}`} color="warning" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip
              label={`Errors: ${validation.errorCount}`}
              color="error"
              icon={<AlertCircle size={14} />}
              sx={{ fontWeight: 600 }}
            />
            <Chip
              label={`Warnings: ${validation.warningCount}`}
              color="warning"
              icon={<AlertTriangle size={14} />}
              variant="outlined"
              sx={{ fontWeight: 600 }}
            />
          </Box>

          {!validation.valid ? (
            <Alert severity="error" sx={{ marginBottom: '16px', borderRadius: '8px' }}>
              This workbook has {validation.errorCount} blocking error(s). Fix them in the file and re-upload, or upload a different file.
            </Alert>
          ) : (
            <Alert severity="success" sx={{ marginBottom: '16px', borderRadius: '8px' }}>
              No blocking errors. {validation.preview.length} student(s) with {totalChanges} change(s) are ready to commit.
            </Alert>
          )}

          {validation.issues.length > 0 ? (
            <>
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '8px' }}>
                <Button
                  variant="outlined"
                  size="small"
                  startIcon={<Download size={15} />}
                  onClick={() => exportBulkIssuesReport(validation.issues)}
                  sx={{ borderColor: '#0D47A1', color: '#0D47A1', borderRadius: '8px', fontWeight: 600 }}
                >
                  Export Issues
                </Button>
              </Box>
              <Box sx={{ maxHeight: '420px', overflow: 'auto', border: isDark ? '1px solid #2D333B' : '1px solid #E6ECF5', borderRadius: '8px' }}>
                <Table size="small" stickyHeader sx={{ minWidth: '720px' }}>
                  <TableHead>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 700 }}>Severity</TableCell>
                      <TableCell sx={{ fontWeight: 700 }}>Sheet</TableCell>
                      <TableCell sx={{ fontWeight: 700 }}>Row</TableCell>
                      <TableCell sx={{ fontWeight: 700 }}>Field</TableCell>
                      <TableCell sx={{ fontWeight: 700 }}>Message</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {validation.issues.map((issue, idx) => (
                      <TableRow key={idx}>
                        <TableCell>
                          <Chip
                            size="small"
                            label={issue.severity}
                            color={severityColor(issue.severity)}
                            icon={issue.severity === 'ERROR' ? <AlertCircle size={13} /> : <AlertTriangle size={13} />}
                          />
                        </TableCell>
                        <TableCell sx={{ color: '#1A2B49' }}>{issue.sheet}</TableCell>
                        <TableCell>{issue.rowNumber}</TableCell>
                        <TableCell sx={{ color: '#1A2B49' }}>{issue.field}</TableCell>
                        <TableCell>{issue.message}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </Box>
            </>
          ) : (
            <Alert severity="success" sx={{ borderRadius: '8px' }}>
              No issues found.
            </Alert>
          )}
        </StepCard>
      )}

      {activeStep === 5 && validation && (
        <StepCard isDark={isDark}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49' }}>
              Preview changes
            </Typography>
            <Button
              variant="outlined"
              size="small"
              startIcon={<Download size={15} />}
              onClick={() => exportBulkPreviewReport(validation.preview)}
              sx={{ borderColor: '#0D47A1', color: '#0D47A1', borderRadius: '8px', fontWeight: 600 }}
            >
              Export Preview
            </Button>
          </Box>
          <Typography variant="body2" sx={{ color: '#667085', marginBottom: '16px' }}>
            {validation.preview.length} student(s) matched with {totalChanges} field change(s) in total.
          </Typography>

          {validation.preview.length === 0 ? (
            <Alert severity="info" sx={{ borderRadius: '8px' }}>
              No students were matched. Verify the Application No / Register No in your workbook.
            </Alert>
          ) : (
            <>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {(visiblePreview ?? []).map((student) => (
                  <Paper
                    key={student.studentId}
                    elevation={0}
                    sx={{
                      borderRadius: '10px',
                      border: isDark ? '1px solid #2D333B' : '1px solid #E6ECF5',
                      backgroundColor: isDark ? '#1C2128' : '#F5F8FC',
                      overflow: 'hidden',
                    }}
                  >
                    <Box
                      sx={{
                        padding: '12px 16px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        gap: '12px',
                        flexWrap: 'wrap',
                      }}
                    >
                      <Box sx={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                        <Typography sx={{ fontWeight: 700, color: '#1A2B49', fontSize: '14px' }}>
                          {student.studentName || 'Unnamed Student'}
                        </Typography>
                        <Typography variant="caption" sx={{ color: '#667085' }}>
                          App No: {student.applicationNo || 'N/A'} · Reg No: {student.registerNo || 'N/A'} · ID: {student.studentId}
                        </Typography>
                      </Box>
                      <Chip
                        size="small"
                        label={`${student.changes.length} change(s)`}
                        color={student.changes.length > 0 ? 'primary' : 'default'}
                        variant="outlined"
                        sx={{ fontWeight: 600 }}
                      />
                    </Box>
                    {student.changes.length > 0 && (
                      <>
                        <Divider />
                        <Table size="small" sx={{ minWidth: '560px' }}>
                          <TableHead>
                            <TableRow>
                              <TableCell sx={{ fontWeight: 700 }}>Sheet</TableCell>
                              <TableCell sx={{ fontWeight: 700 }}>Field</TableCell>
                              <TableCell sx={{ fontWeight: 700 }}>Current Value</TableCell>
                              <TableCell sx={{ fontWeight: 700 }}>New Value</TableCell>
                            </TableRow>
                          </TableHead>
                          <TableBody>
                            {student.changes.map((change, idx) => (
                              <TableRow key={idx}>
                                <TableCell sx={{ color: '#1A2B49' }}>{change.sheet}</TableCell>
                                <TableCell sx={{ color: '#1A2B49', fontWeight: 500 }}>{change.field}</TableCell>
                                <TableCell sx={{ color: '#667085' }}>{change.oldValue || '—'}</TableCell>
                                <TableCell sx={{ color: '#0D47A1', fontWeight: 600 }}>{change.newValue || '—'}</TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </>
                    )}
                  </Paper>
                ))}
              </Box>

              {validation.preview.length > (visiblePreview?.length ?? 0) && (
                <Box sx={{ display: 'flex', justifyContent: 'center', marginTop: '16px' }}>
                  <Button
                    variant="outlined"
                    onClick={() => setPreviewPage((p) => p + 1)}
                    sx={{ borderColor: '#0D47A1', color: '#0D47A1', borderRadius: '8px', fontWeight: 600 }}
                  >
                    Load More
                  </Button>
                </Box>
              )}
            </>
          )}
        </StepCard>
      )}

      {activeStep === 6 && validation && (
        <StepCard isDark={isDark}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49', marginBottom: '16px' }}>
            Confirm & commit
          </Typography>
          <Alert severity="info" sx={{ marginBottom: '16px', borderRadius: '8px' }}>
            The whole workbook is committed in a single transaction. If any row is invalid, nothing is written to the database.
          </Alert>
          <Box sx={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '8px' }}>
            <Chip label={`File: ${validation.fileName || parseResult?.fileName}`} color="primary" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Total Rows: ${validation.totalRows}`} variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Students to Update: ${validation.matchedStudents}`} color="success" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Unmatched: ${validation.unmatchedRows}`} color="warning" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Field Changes: ${totalChanges}`} color="primary" variant="outlined" sx={{ fontWeight: 600 }} />
            {warningIssues.length > 0 && (
              <Chip label={`Warnings: ${warningIssues.length}`} color="warning" variant="outlined" sx={{ fontWeight: 600 }} />
            )}
          </Box>
          <Typography variant="body2" sx={{ color: '#667085', marginTop: '8px' }}>
            On success, an audit entry is saved to the bulk upload log with the summary counts.
          </Typography>
        </StepCard>
      )}

      {activeStep === 7 && commitResponse && (
        <StepCard isDark={isDark}>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              marginBottom: '16px',
            }}
          >
            {commitResponse.status === 'SUCCESS' ? (
              <CheckCircle2 size={36} color="#22A06B" />
            ) : (
              <AlertCircle size={36} color="#D92D20" />
            )}
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#1A2B49' }}>
                {commitResponse.status === 'SUCCESS' ? 'Update completed successfully' : 'Update failed'}
              </Typography>
              <Typography variant="body2" sx={{ color: '#667085' }}>
                {commitResponse.status === 'SUCCESS'
                  ? 'All valid rows were applied in a single transaction.'
                  : 'No changes were applied. Review the blocking errors below.'}
              </Typography>
            </Box>
          </Box>

          <Box sx={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '16px' }}>
            <Chip
              label={`Status: ${commitResponse.status}`}
              color={statusChipColor(commitResponse.status)}
              sx={{ fontWeight: 600 }}
            />
            <Chip label={`Total Rows: ${commitResponse.totalRows}`} color="primary" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Valid Rows: ${commitResponse.validRows}`} color="success" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Students Updated: ${commitResponse.updatedStudents}`} color="success" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`No Change: ${commitResponse.noChangeRows}`} color="default" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Skipped: ${commitResponse.skippedRows}`} color="warning" variant="outlined" sx={{ fontWeight: 600 }} />
            <Chip label={`Failed: ${commitResponse.failedRows}`} color="error" variant="outlined" sx={{ fontWeight: 600 }} />
            {commitResponse.warningCount > 0 && (
              <Chip label={`Warnings: ${commitResponse.warningCount}`} color="warning" variant="outlined" sx={{ fontWeight: 600 }} />
            )}
          </Box>

          <Typography variant="body2" sx={{ color: '#667085', marginBottom: '4px' }}>
            Duration: {(commitResponse.durationMs / 1000).toFixed(2)} s · Uploaded By: {commitResponse.uploadedBy || 'Admin'} · {commitResponse.uploadedAt ? new Date(commitResponse.uploadedAt).toLocaleString() : ''}
          </Typography>

          {commitResponse.issues.length > 0 && (
            <Box sx={{ marginTop: '12px', maxHeight: '260px', overflow: 'auto', border: isDark ? '1px solid #2D333B' : '1px solid #E6ECF5', borderRadius: '8px' }}>
              <Table size="small" stickyHeader sx={{ minWidth: '720px' }}>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700 }}>Severity</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Sheet</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Row</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Field</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Message</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {commitResponse.issues.map((issue, idx) => (
                    <TableRow key={idx}>
                      <TableCell>
                        <Chip
                          size="small"
                          label={issue.severity}
                          color={severityColor(issue.severity)}
                          icon={issue.severity === 'ERROR' ? <AlertCircle size={13} /> : <AlertTriangle size={13} />}
                        />
                      </TableCell>
                      <TableCell sx={{ color: '#1A2B49' }}>{issue.sheet}</TableCell>
                      <TableCell>{issue.rowNumber}</TableCell>
                      <TableCell sx={{ color: '#1A2B49' }}>{issue.field}</TableCell>
                      <TableCell>{issue.message}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Box>
          )}

          <Divider sx={{ margin: '20px 0' }} />

          <Box sx={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <Button
              variant="contained"
              startIcon={<Download size={16} />}
              onClick={() => exportBulkCommitReport(commitResponse)}
              sx={{ backgroundColor: '#0D47A1', borderRadius: '8px', fontWeight: 600 }}
            >
              Download Report
            </Button>
            {commitResponse.issues.length > 0 && (
              <Button
                variant="outlined"
                startIcon={<Download size={16} />}
                onClick={() => exportBulkIssuesReport(commitResponse.issues)}
                sx={{ borderColor: '#0D47A1', color: '#0D47A1', borderRadius: '8px', fontWeight: 600 }}
              >
                Download Issues
              </Button>
            )}
            <Button
              variant="outlined"
              startIcon={<RefreshCcw size={16} />}
              onClick={handleReset}
              sx={{ borderColor: '#0D47A1', color: '#0D47A1', borderRadius: '8px', fontWeight: 600 }}
            >
              Start New Bulk Update
            </Button>
            <Button
              variant="outlined"
              startIcon={<ArrowRight size={16} />}
              onClick={() => navigate('/')}
              sx={{ borderColor: '#D8E4F2', color: '#667085', borderRadius: '8px', fontWeight: 600 }}
            >
              Back to Dashboard
            </Button>
          </Box>
        </StepCard>
      )}

      {/* Bottom action bar */}
      {activeStep < 7 && (
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            marginTop: '20px',
            padding: '16px',
            borderRadius: '12px',
            backgroundColor: isDark ? '#161B22' : '#FFFFFF',
            border: isDark ? '1px solid #2D333B' : '1px solid #E6ECF5',
          }}
        >
          <Button
            variant="outlined"
            startIcon={<ArrowLeft size={16} />}
            onClick={() => go(Math.max(0, activeStep - 1))}
            disabled={activeStep === 0}
            sx={{ borderColor: '#D8E4F2', color: '#667085', borderRadius: '8px', fontWeight: 600 }}
          >
            Back
          </Button>

          {activeStep === 3 && (
            <Button
              variant="contained"
              startIcon={isValidating ? <CircularProgress size={16} color="inherit" /> : <CheckCircle2 size={16} />}
              onClick={handleValidate}
              disabled={isValidating}
              sx={{ backgroundColor: '#0D47A1', borderRadius: '8px', fontWeight: 600, padding: '8px 20px' }}
            >
              {isValidating ? 'Validating...' : 'Validate Workbook'}
            </Button>
          )}

          {activeStep !== 3 && (
            <Button
              variant="contained"
              startIcon={isCommitting && activeStep === 6 ? <CircularProgress size={16} color="inherit" /> : undefined}
              endIcon={isCommitting && activeStep === 6 ? undefined : <ArrowRight size={16} />}
              onClick={handleNext}
              disabled={!canNext() || isCommitting}
              sx={{ backgroundColor: '#0D47A1', borderRadius: '8px', fontWeight: 600, padding: '8px 20px' }}
            >
              {activeStep === 6 ? (isCommitting ? 'Committing...' : 'Commit Updates') : 'Next'}
            </Button>
          )}
        </Box>
      )}
    </Box>
  );
};
