import React from 'react';
import { Grid, TextField, Typography, Divider, Box } from '@mui/material';
import { EventFormData } from '../types';

interface Props {
  formData: EventFormData;
  onChange: (e: React.ChangeEvent<any>) => void;
  disabled?: boolean;
}

const EventOrganizerSection: React.FC<Props> = ({ formData, onChange, disabled }) => {
  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
        Organizer Information
      </Typography>
      <Divider sx={{ mb: 3 }} />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 4 }}>
          <TextField fullWidth label="Organizer Name" name="organizerName" value={formData.organizerName} onChange={onChange} disabled={disabled} />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <TextField fullWidth label="Organizer Phone" name="organizerPhone" value={formData.organizerPhone} onChange={onChange} disabled={disabled} />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <TextField fullWidth label="Organizer Email" name="organizerEmail" type="email" value={formData.organizerEmail} onChange={onChange} disabled={disabled} />
        </Grid>
      </Grid>
    </Box>
  );
};

export default EventOrganizerSection;
