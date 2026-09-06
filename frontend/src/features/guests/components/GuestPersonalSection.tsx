/**
 * GuestPersonalSection
 * Purpose: Collects personal information fields.
 * Fields: First Name*, Last Name*, Gender, Date of Birth.
 */
import React from 'react';
import { Grid, MenuItem, TextField } from '@mui/material';

export interface PersonalInfo {
    firstName: string;
    lastName: string;
    gender: string; // 'Male' | 'Female' | '' (keep simple for MVP)
    dateOfBirth: string; // ISO date (yyyy-mm-dd) as string
}

interface Props {
    value: PersonalInfo;
    onChange: (v: PersonalInfo) => void;
    readOnly?: boolean;
    errors?: { firstName?: string; lastName?: string };
}

const GuestPersonalSection: React.FC<Props> = ({ value, onChange, readOnly, errors }) => {
    const handle = (key: keyof PersonalInfo) => (e: React.ChangeEvent<HTMLInputElement>) =>
        onChange({ ...value, [key]: e.target.value });

    return (
        <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                    label="First Name"
                    value={value.firstName}
                    onChange={handle('firstName')}
                    required
                    fullWidth
                    slotProps={{ input: { readOnly } }}
                    error={!!errors?.firstName}
                    helperText={errors?.firstName}
                />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                    label="Last Name"
                    value={value.lastName}
                    onChange={handle('lastName')}
                    required
                    fullWidth
                    slotProps={{ input: { readOnly } }}
                    error={!!errors?.lastName}
                    helperText={errors?.lastName}
                />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                    label="Gender"
                    value={value.gender}
                    onChange={handle('gender')}
                    select
                    fullWidth
                    slotProps={{ input: { readOnly } }}
                >
                    <MenuItem value="">-</MenuItem>
                    <MenuItem value="Male">Male</MenuItem>
                    <MenuItem value="Female">Female</MenuItem>
                </TextField>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                    label="Date of Birth"
                    type="date"
                    value={value.dateOfBirth}
                    onChange={handle('dateOfBirth')}
                    fullWidth
                    slotProps={{
                        inputLabel: { shrink: true },
                        input: { readOnly },
                    }}
                />
            </Grid>
        </Grid>
    );
};

export default GuestPersonalSection;
