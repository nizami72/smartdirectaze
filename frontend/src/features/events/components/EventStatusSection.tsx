import React from 'react';
import { Box, Typography, Divider, Grid, FormControl, FormLabel, RadioGroup, FormControlLabel, Radio } from '@mui/material';
import { EventFormData, EventStatus, EventVisibility } from '../types';

interface Props {
  formData: EventFormData;
  onChange: (e: React.ChangeEvent<any>) => void;
  disabled?: boolean;
}

const EventStatusSection: React.FC<Props> = ({ formData, onChange, disabled }) => {
  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
        Event Status
      </Typography>
      <Divider sx={{ mb: 3 }} />
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <FormControl component="fieldset" disabled={disabled}>
            <FormLabel component="legend">Visibility</FormLabel>
            <RadioGroup row name="visibility" value={formData.visibility} onChange={onChange}>
              <FormControlLabel value={EventVisibility.PUBLIC} control={<Radio />} label="Public" />
              <FormControlLabel value={EventVisibility.PRIVATE} control={<Radio />} label="Private" />
            </RadioGroup>
          </FormControl>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <FormControl component="fieldset" disabled={disabled}>
            <FormLabel component="legend">Status</FormLabel>
            <RadioGroup row name="status" value={formData.status} onChange={onChange}>
              <FormControlLabel value={EventStatus.DRAFT} control={<Radio />} label="Draft" />
              <FormControlLabel value={EventStatus.PUBLISHED} control={<Radio />} label="Published" />
            </RadioGroup>
          </FormControl>
        </Grid>
      </Grid>
    </Box>
  );
};

export default EventStatusSection;
