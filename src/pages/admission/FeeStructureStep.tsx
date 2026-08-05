import React, { useState, useEffect } from 'react';
import {
  Grid,
  TextField,
  MenuItem,
  Typography,
  Box,
  Switch,
  FormControlLabel,
  InputAdornment,
} from '@mui/material';
import { useAdmission } from '../../context/AdmissionContext';
import { AppCard } from '../../components/ui/AppCard';
import { SummaryCard } from '../../components/ui/SummaryCard';
import { BUS_ROUTES_WITH_STOPS } from '../../utils/constants';
import { calculateFeeDetails } from '../../utils/feeCalculator';
import { AdmissionCategory, ProgramType } from '../../types';

const fieldSx = {
  '& .MuiInputLabel-root': {
    fontSize: '14.5px',
    fontWeight: 600,
    color: '#344054',
    transform: 'translate(14px, 14px) scale(1)',
  },
  '& .MuiInputLabel-shrink': {
    transform: 'translate(14px, -9px) scale(0.75)',
  },
  '& .MuiOutlinedInput-root': {
    height: '50px',
    borderRadius: '12px',
    fontSize: '15px',
    '& input': {
      padding: '13px 16px',
    },
    '& input::placeholder': {
      fontSize: '14.5px',
    },
    '& .MuiSelect-select': {
      padding: '13px 16px',
      display: 'flex',
      alignItems: 'center',
    },
    '& .MuiSvgIcon-root': {
      fontSize: '20px',
    },
  },
};

export const FeeStructureStep: React.FC<{ onNext: () => void }> = ({ onNext }) => {
  const { draftStudent, updateDraftSection, isViewReadOnly } = useAdmission();

  const category = draftStudent.academic?.admissionCategory;
  const program = draftStudent.academic?.program;
  const initialCutoff = draftStudent.fee?.cutOffMark;

  const [busTransport, setBusTransport] = useState<boolean>(
    draftStudent.fee?.busTransportRequired || false
  );
  const [busRoute, setBusRoute] = useState<string>(
    draftStudent.fee?.busRouteSelected || ''
  );
  const [busStop, setBusStop] = useState<string>(
    draftStudent.fee?.busStopSelected || ''
  );
  const [hostel, setHostel] = useState<boolean>(
    draftStudent.fee?.hostelRequired || false
  );

  const feeDetails = calculateFeeDetails(
    category,
    program,
    initialCutoff,
    busTransport,
    busTransport ? busRoute : '',
    busTransport ? busStop : '',
    hostel
  );

  const availableStops = busRoute ? (BUS_ROUTES_WITH_STOPS[busRoute] || []) : [];

  useEffect(() => {
    updateDraftSection('fee', feeDetails);
  }, [busTransport, busRoute, busStop, hostel]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    updateDraftSection('fee', feeDetails);
    onNext();
  };

  return (
    <Box component="form" id="wizard-step-form" onSubmit={handleSubmit}>
      <AppCard>
        <Typography sx={{ fontWeight: 700, color: '#0D47A1', marginBottom: '4px', fontSize: '22px' }}>
          Fee Structure & Optional Facilities
        </Typography>
        <Typography sx={{ color: '#667085', marginBottom: '24px', fontSize: '14px' }}>
          Assign the student's tuition fee and select optional hostel/bus transport routes.
        </Typography>

        <Grid container columnSpacing={3} rowSpacing={2.5}>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Cut-Off Mark / Percentage"
              fullWidth
              sx={fieldSx}
              disabled
              value={feeDetails.cutOffMark !== undefined ? feeDetails.cutOffMark : 0}
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <TextField
              label="Tuition Fee Per Year (Auto Populated)"
              fullWidth
              sx={fieldSx}
              disabled
              value={feeDetails.tuitionFeePerYear !== undefined ? feeDetails.tuitionFeePerYear.toLocaleString('en-IN') : '0'}
              InputProps={{
                startAdornment: <InputAdornment position="start" sx={{ '& .MuiTypography-root': { fontSize: '15px' } }}>₹</InputAdornment>,
              }}
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <TextField
              label="Course Duration (Years)"
              fullWidth
              sx={fieldSx}
              disabled
              value={program ? `${feeDetails.courseDurationYears} Years (${program})` : '0 Years'}
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <TextField
              label="Total Tuition Fee"
              fullWidth
              sx={fieldSx}
              disabled
              value={feeDetails.totalTuitionFee !== undefined ? feeDetails.totalTuitionFee.toLocaleString('en-IN') : '0'}
              InputProps={{
                startAdornment: <InputAdornment position="start" sx={{ '& .MuiTypography-root': { fontSize: '15px' } }}>₹</InputAdornment>,
              }}
            />
          </Grid>

          {/* Bus Transport Enable Toggle */}
          <Grid item xs={12} sm={6}>
            <Box
              sx={{
                border: '1px solid #D8E4F2',
                borderRadius: '12px',
                padding: '4px 16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                height: '50px',
              }}
            >
              <Typography sx={{ fontWeight: 600, color: '#1A2B49', fontSize: '14.5px' }}>
                Bus Transport
              </Typography>
              <FormControlLabel
                control={
                  <Switch
                    checked={busTransport}
                    onChange={(e) => {
                      const enabled = e.target.checked;
                      setBusTransport(enabled);
                      if (!enabled) {
                        setBusRoute('');
                        setBusStop('');
                      }
                    }}
                    disabled={isViewReadOnly}
                    color="primary"
                    size="small"
                  />
                }
                label={busTransport ? 'ON' : 'OFF (Default)'}
                sx={{ margin: 0, '& .MuiFormControlLabel-label': { fontSize: '13px', fontWeight: 600 } }}
              />
            </Box>
          </Grid>

          {/* Hostel Accommodation Enable Toggle */}
          <Grid item xs={12} sm={6}>
            <Box
              sx={{
                border: '1px solid #D8E4F2',
                borderRadius: '12px',
                padding: '4px 16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                height: '50px',
              }}
            >
              <Typography sx={{ fontWeight: 600, color: '#1A2B49', fontSize: '14.5px' }}>
                Hostel Accommodation
              </Typography>
              <FormControlLabel
                control={
                  <Switch
                    checked={hostel}
                    onChange={(e) => setHostel(e.target.checked)}
                    disabled={isViewReadOnly}
                    color="primary"
                    size="small"
                  />
                }
                label={hostel ? 'Required (₹72,000/yr)' : 'Not Required'}
                sx={{ margin: 0, '& .MuiFormControlLabel-label': { fontSize: '13px', fontWeight: 600 } }}
              />
            </Box>
          </Grid>

          {/* Dynamic Bus Route Dropdown */}
          {busTransport && (
            <>
              <Grid item xs={12} sm={6}>
                <TextField
                  select
                  label="Bus Route *"
                  fullWidth
                  sx={fieldSx}
                  disabled={isViewReadOnly}
                  value={busRoute}
                  onChange={(e) => {
                    setBusRoute(e.target.value);
                    setBusStop(''); // Reset bus stop on route change
                  }}
                >
                  {Object.keys(BUS_ROUTES_WITH_STOPS).map((route) => (
                    <MenuItem key={route} value={route}>
                      {route}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>

              {/* Dynamic Bus Stop Dropdown */}
              <Grid item xs={12} sm={6}>
                <TextField
                  select
                  label="Bus Stop *"
                  fullWidth
                  sx={fieldSx}
                  disabled={isViewReadOnly || !busRoute}
                  value={busStop}
                  onChange={(e) => setBusStop(e.target.value)}
                >
                  {availableStops.map((stop) => (
                    <MenuItem key={stop.stopName} value={stop.stopName}>
                      {stop.stopName} (₹{stop.fee.toLocaleString('en-IN')})
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
            </>
          )}
        </Grid>

        <SummaryCard
          title="Grand Total Fee Summary"
          items={[
            { label: 'Calculated Cut-Off', value: `${feeDetails.cutOffMark || 0}%` },
            { label: `Tuition Fee (${feeDetails.courseDurationYears || 0} Years Total)`, value: `₹ ${(feeDetails.totalTuitionFee || 0).toLocaleString('en-IN')}` },
            { label: 'Bus Route Fee', value: `₹ ${(feeDetails.busFee || 0).toLocaleString('en-IN')}` },
            { label: 'Hostel Accommodation Fee', value: `₹ ${(feeDetails.hostelFee || 0).toLocaleString('en-IN')}` },
            { label: 'GRAND TOTAL FEE', value: `₹ ${(feeDetails.grandTotalFee || 0).toLocaleString('en-IN')}`, isHighlight: true },
          ]}
        />
      </AppCard>
    </Box>
  );
};
