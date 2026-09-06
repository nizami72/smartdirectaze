/**
 * GuestSearch
 * Purpose: Controlled search input used to filter guests.
 * User interactions in this component:
 * - Typing in the text field updates the search value via onChange.
 */
import React from 'react';
import { TextField } from '@mui/material';

interface Props {
  value: string;
  onChange: (v: string) => void;
}

const GuestSearch: React.FC<Props> = ({ value, onChange }) => {
  return (
    <TextField
      fullWidth
      placeholder="Search by name, phone, WhatsApp, email"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      size="small"
    />
  );
};

export default GuestSearch;
