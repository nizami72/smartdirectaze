import React from 'react';
import { Grid, TextField, Typography, Divider, Box } from '@mui/material';
import { EventFormData } from '../types';

interface ContactSectionProps {
  formData: EventFormData;
  onChange: (e: React.ChangeEvent<any>) => void;
}

const ContactSection: React.FC<ContactSectionProps> = ({ formData, onChange }) => {
  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
        Contact Information
      </Typography>
      <Divider sx={{ mb: 3 }} />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 4 }}>
          <TextField
            fullWidth
            label="Organizer Name"
            name="organizerName"
            value={formData.organizerName}
            onChange={onChange}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <TextField
            fullWidth
            label="Organizer Phone"
            name="organizerPhone"
            value={formData.organizerPhone}
            onChange={onChange}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <TextField
            fullWidth
            label="Organizer Email"
            name="organizerEmail"
            type="email"
            value={formData.organizerEmail}
            onChange={onChange}
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default ContactSection;
