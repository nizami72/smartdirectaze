/**
 * GuestAddressSection
 * Purpose: Collects address information fields.
 * Fields: Country, City, Address.
 */
import React from 'react';
import { Grid, TextField } from '@mui/material';

export interface AddressInfo {
    country: string;
    city: string;
    address: string;
}

interface Props {
    value: AddressInfo;
    onChange: (v: AddressInfo) => void;
    readOnly?: boolean;
}

const GuestAddressSection: React.FC<Props> = ({ value, onChange, readOnly }) => {
    const handle = (key: keyof AddressInfo) => (e: React.ChangeEvent<HTMLInputElement>) =>
        onChange({ ...value, [key]: e.target.value });

    return (
        <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6 }}>
                <TextField label="Country" value={value.country} onChange={handle('country')} fullWidth slotProps={{ input: { readOnly } }} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
                <TextField label="City" value={value.city} onChange={handle('city')} fullWidth slotProps={{ input: { readOnly } }} />
            </Grid>
            <Grid size={{ xs: 12 }}>
                <TextField label="Address" value={value.address} onChange={handle('address')} fullWidth slotProps={{ input: { readOnly } }} />
            </Grid>
        </Grid>
    );
};

export default GuestAddressSection;
