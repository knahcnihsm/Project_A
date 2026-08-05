import { z } from 'zod';

export const studentDetailsSchema = z.object({
  applicationNumber: z.string().min(1, 'Application Number is required'),
  registerNumber: z.string().min(1, 'Register Number is required'),
  studentName: z.string().min(1, 'Student Name is required'),
  dateOfBirth: z.string().min(1, 'Date of Birth is required'),
  age: z.number().min(16, 'Age must be at least 16'),
  aadhaarNumber: z
    .string()
    .regex(/^\d{12}$/, 'Aadhaar Number must be exactly 12 digits'),
  gender: z.enum(['Male', 'Female', 'Transgender'], {
    errorMap: () => ({ message: 'Gender is required' }),
  }),
  district: z.string().min(1, 'District is required'),
  nationality: z.string().min(1, 'Nationality is required'),
  caste: z.string().min(1, 'Caste / Category is required'),
});

export type StudentDetailsFormData = z.infer<typeof studentDetailsSchema>;
