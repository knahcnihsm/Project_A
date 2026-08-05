import React from 'react';
import { Grid, TextField, Typography, Box, InputAdornment } from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { parentDetailsSchema, ParentDetailsFormData } from '../../schemas/parent.schema';
import { useAdmission } from '../../context/AdmissionContext';
import { AppCard } from '../../components/ui/AppCard';

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
    '& .MuiSvgIcon-root': {
      fontSize: '20px',
    },
  },
};

export const ParentDetailsStep: React.FC<{ onNext: () => void }> = ({ onNext }) => {
  const { draftStudent, updateDraftSection, isViewReadOnly } = useAdmission();

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<ParentDetailsFormData>({
    resolver: zodResolver(parentDetailsSchema),
    defaultValues: {
      fatherName: draftStudent.parent?.fatherName || '',
      fatherMobile: draftStudent.parent?.fatherMobile || '',
      fatherOccupation: draftStudent.parent?.fatherOccupation || '',
      annualIncome: draftStudent.parent?.annualIncome ?? undefined,
    },
  });

  const onSubmit = (data: ParentDetailsFormData) => {
    updateDraftSection('parent', {
      ...data,
      fatherOccupation: data.fatherOccupation || '',
    });
    onNext();
  };

  return (
    <Box component="form" id="wizard-step-form" onSubmit={handleSubmit(onSubmit)}>
      <AppCard>
        <Typography sx={{ fontWeight: 700, color: '#0D47A1', marginBottom: '4px', fontSize: '22px' }}>
          Parent / Guardian Details
        </Typography>
        <Typography sx={{ color: '#667085', marginBottom: '24px', fontSize: '14px' }}>
          Enter the parent's information for official admission records.
        </Typography>

        <Grid container columnSpacing={3} rowSpacing={2.5}>
          <Grid item xs={12} sm={6}>
            <Controller
              name="fatherName"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Father / Guardian Name *"
                  fullWidth
                  sx={fieldSx}
                  disabled={isViewReadOnly}
                  error={!!errors.fatherName}
                  helperText={errors.fatherName?.message}
                />
              )}
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <Controller
              name="fatherMobile"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Father Mobile Number (10 Digits) *"
                  fullWidth
                  sx={fieldSx}
                  disabled={isViewReadOnly}
                  inputProps={{ maxLength: 10 }}
                  error={!!errors.fatherMobile}
                  helperText={errors.fatherMobile?.message}
                />
              )}
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <Controller
              name="fatherOccupation"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Father Occupation"
                  fullWidth
                  sx={fieldSx}
                  disabled={isViewReadOnly}
                  error={!!errors.fatherOccupation}
                  helperText={errors.fatherOccupation?.message}
                />
              )}
            />
          </Grid>

          <Grid item xs={12} sm={6}>
            <Controller
              name="annualIncome"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  value={field.value !== undefined && field.value !== null ? field.value : ''}
                  type="number"
                  label="Annual Family Income *"
                  fullWidth
                  sx={fieldSx}
                  disabled={isViewReadOnly}
                  InputProps={{
                    startAdornment: <InputAdornment position="start" sx={{ '& .MuiTypography-root': { fontSize: '15px' } }}>₹</InputAdornment>,
                  }}
                  onChange={(e) => {
                    const val = e.target.value;
                    field.onChange(val === '' ? undefined : Number(val));
                  }}
                  error={!!errors.annualIncome}
                  helperText={errors.annualIncome?.message}
                />
              )}
            />
          </Grid>
        </Grid>
      </AppCard>
    </Box>
  );
};
