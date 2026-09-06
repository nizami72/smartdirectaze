/**
 * GuestActions
 * Purpose: Compact action buttons used inside the Guest row.
 * User interactions in this component:
 * - View button: opens guest details route.
 * - Edit button: opens guest edit route (query param mode=edit in MVP).
 * - Delete button (business mode): placeholder destructive action (no-op in MVP).
 * - Remove From Event button (event mode): placeholder to unlink guest from event (no-op in MVP).
 */
import React from 'react';
import { IconButton, Stack, Tooltip } from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import PersonRemoveAlt1Icon from '@mui/icons-material/PersonRemoveAlt1';

type Mode = 'all' | 'event';

interface Props {
  mode: Mode;
  onView: () => void;
  onEdit: () => void;
  onDelete?: () => void;
  onRemoveFromEvent?: () => void;
}

const GuestActions: React.FC<Props> = ({ mode, onView, onEdit, onDelete, onRemoveFromEvent }) => {
  return (
    <Stack direction="row" spacing={0.5}>
      <Tooltip title="View"><span>
        <IconButton size="small" onClick={onView}>
          <VisibilityIcon fontSize="small" />
        </IconButton>
      </span></Tooltip>
      <Tooltip title="Edit"><span>
        <IconButton size="small" onClick={onEdit}>
          <EditIcon fontSize="small" />
        </IconButton>
      </span></Tooltip>
      {mode === 'business' ? (
        <Tooltip title="Delete"><span>
          <IconButton size="small" color="error" onClick={onDelete}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </span></Tooltip>
      ) : (
        <Tooltip title="Remove From Event"><span>
          <IconButton size="small" color="error" onClick={onRemoveFromEvent}>
            <PersonRemoveAlt1Icon fontSize="small" />
          </IconButton>
        </span></Tooltip>
      )}
    </Stack>
  );
};

export default GuestActions;
