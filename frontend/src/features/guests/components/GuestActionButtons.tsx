/**
 * GuestActionButtons
 * Purpose: Renders action buttons based on page mode.
 * Modes:
 * - create: Save, Cancel
 * - view: Edit, Delete
 * - edit: Save, Cancel, Delete
 */
import React from 'react';
import { Button, Stack } from '@mui/material';

type Mode = 'create' | 'view' | 'edit';

interface Props {
  mode: Mode;
  onEdit: () => void;
  onSave: () => void;
  onCancel: () => void;
  onDelete: () => void;
  saving?: boolean;
}

const GuestActionButtons: React.FC<Props> = ({ mode, onEdit, onSave, onCancel, onDelete, saving }) => {
  if (mode === 'create') {
    return (
      <Stack direction="row" spacing={1}>
        <Button variant="contained" onClick={onSave} disabled={!!saving}>{saving ? 'Saving…' : 'Save'}</Button>
        <Button variant="text" onClick={onCancel} disabled={!!saving}>Cancel</Button>
      </Stack>
    );
  }
  if (mode === 'view') {
    return (
      <Stack direction="row" spacing={1}>
        <Button variant="contained" onClick={onEdit}>Edit</Button>
        <Button color="error" variant="outlined" onClick={onDelete}>Delete</Button>
      </Stack>
    );
  }
  // edit
  return (
    <Stack direction="row" spacing={1}>
      <Button variant="contained" onClick={onSave} disabled={!!saving}>{saving ? 'Saving…' : 'Save'}</Button>
      <Button variant="text" onClick={onCancel} disabled={!!saving}>Cancel</Button>
      <Button color="error" variant="outlined" onClick={onDelete} disabled={!!saving}>Delete</Button>
    </Stack>
  );
};

export default GuestActionButtons;
