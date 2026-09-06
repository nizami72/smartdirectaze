import React from 'react';
import { Grid, TextField, Typography, Divider, Box } from '@mui/material';
import { EventFormData } from '../types';

interface AdditionalInformationSectionProps {
  formData: EventFormData;
  onChange: (e: React.ChangeEvent<any>) => void;
}

const AdditionalInformationSection: React.FC<AdditionalInformationSectionProps> = ({ 
  formData, 
  onChange 
}) => {
  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
        Additional Information
      </Typography>
      <Divider sx={{ mb: 3 }} />
      <Grid container spacing={3}>
        <Grid size={12}>
          <TextField
            fullWidth
            label="Notes"
            name="notes"
            value={formData.notes}
            onChange={onChange}
            multiline
            rows={4}
            placeholder="Add any additional notes or requirements for the event..."
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default AdditionalInformationSection;
