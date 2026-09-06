import React from 'react';
import { Box, Button, CircularProgress } from '@mui/material';

type Mode = 'create' | 'view' | 'edit';

interface Props {
  mode: Mode;
  isSubmitting: boolean;
  onSave: () => void;
  onCancel: () => void;
  onDelete: () => void;
  onEdit: () => void;
  onViewGuests: () => void;
  onSendInvitations: () => void;
}

const EventActionButtons: React.FC<Props> = ({
  mode,
  isSubmitting,
  onSave,
  onCancel,
  onDelete,
  onEdit,
  onViewGuests,
  onSendInvitations,
}) => {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 4, flexWrap: 'wrap' }}>
      {mode === 'create' && (
        <>
          <Button variant="outlined" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button variant="contained" color="primary" onClick={onSave} disabled={isSubmitting} sx={{ minWidth: 120 }}>
            {isSubmitting ? <CircularProgress size={24} color="inherit" /> : 'Save'}
          </Button>
        </>
      )}

      {mode === 'view' && (
        <>
          <Button variant="outlined" onClick={onViewGuests}>View Guests</Button>
          <Button variant="outlined" color="secondary" onClick={onSendInvitations}>
            Send Invitations
          </Button>
          <Button variant="contained" color="primary" onClick={onEdit}>
            Edit
          </Button>
          <Button variant="outlined" color="error" onClick={onDelete}>
            Delete
          </Button>
        </>
      )}

      {mode === 'edit' && (
        <>
          <Button variant="outlined" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button variant="contained" color="primary" onClick={onSave} disabled={isSubmitting} sx={{ minWidth: 120 }}>
            {isSubmitting ? <CircularProgress size={24} color="inherit" /> : 'Save'}
          </Button>
          <Button variant="outlined" color="error" onClick={onDelete} disabled={isSubmitting}>
            Delete
          </Button>
        </>
      )}
    </Box>
  );
};

export default EventActionButtons;
