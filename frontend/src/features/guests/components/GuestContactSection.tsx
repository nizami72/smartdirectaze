/**
 * GuestContactSection
 * Purpose: Collects contact information fields.
 * Fields: Phone, WhatsApp, Email with basic validation hints.
 */
import React from 'react';
import { Grid, TextField } from '@mui/material';

export interface ContactInfo {
    phone: string;
    whatsapp: string;
    email: string;
}

interface Props {
    value: ContactInfo;
    onChange: (v: ContactInfo) => void;
    readOnly?: boolean;
    errors?: { email?: string; phone?: string };
}

const GuestContactSection: React.FC<Props> = ({ value, onChange, readOnly, errors }) => {
    const handle = (key: keyof ContactInfo) => (e: React.ChangeEvent<HTMLInputElement>) =>
        onChange({ ...value, [key]: e.target.value });

    return (
        <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                    label="Phone"
                    value={value.phone}
                    onChange={handle('phone')}
                    fullWidth
                    slotProps={{ input: { readOnly } }}
                    error={!!errors?.phone}
                    helperText={errors?.phone}
                />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                    label="WhatsApp"
                    value={value.whatsapp}
                    onChange={handle('whatsapp')}
                    fullWidth
                    slotProps={{ input: { readOnly } }}
                />
            </Grid>
            <Grid size={{ xs: 12, sm: 12 }}>
                <TextField
                    label="Email"
                    value={value.email}
                    onChange={handle('email')}
                    fullWidth
                    slotProps={{ input: { readOnly } }}
                    error={!!errors?.email}
                    helperText={errors?.email}
                />
            </Grid>
        </Grid>
    );
};

export default GuestContactSection;
