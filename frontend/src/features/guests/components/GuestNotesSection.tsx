/**
 * GuestNotesSection
 * Purpose: Multiline notes field.
 */
import React from 'react';
import { TextField } from '@mui/material';

interface Props {
  value: string;
  onChange: (v: string) => void;
  readOnly?: boolean;
}

const GuestNotesSection: React.FC<Props> = ({ value, onChange, readOnly }) => {
  return (
      <TextField
          label="Notes"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          fullWidth
          multiline
          minRows={3}
          slotProps={{ input: { readOnly } }}
      />
  );
};

export default GuestNotesSection;
