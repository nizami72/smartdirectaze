/**
 * EmptyGuestState
 * Purpose: Friendly empty state shown when the guest list has no items.
 * User interactions in this component:
 * - "Add First Guest" button: triggers provided onAdd callback to start creating a new guest.
 */
import React from 'react';
import { Box, Button, Typography } from '@mui/material';

type Mode = 'all' | 'event';

interface Props {
  mode: Mode;
  onAdd: () => void;
}

const EmptyGuestState: React.FC<Props> = ({ mode, onAdd }) => {
  const message =
    mode === 'event'
      ? 'No guests have been added to this event.'
      : 'No guests have been created yet.';
  const button = mode === 'event' ? 'Add Guest' : 'Add First Guest';
  return (
    <Box
      sx={{
        py: 8,
        textAlign: 'center',
        color: 'text.secondary',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 2,
      }}
    >
      <Typography variant="subtitle1">{message}</Typography>
      <Button variant="contained" onClick={onAdd}>{button}</Button>
    </Box>
  );
};

export default EmptyGuestState;
