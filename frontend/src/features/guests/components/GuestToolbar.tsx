/**
 * GuestToolbar
 * Purpose: Top toolbar for Guests list with search and primary actions.
 * User interactions in this component:
 * - Search text field: updates query for filtering guests.
 * - "Add Existing Guest" (event mode only): placeholder action (console log in MVP).
 * - Primary button: "Add Guest" (business) or "Create New Guest" (event), triggers onPrimaryAction.
 */
import React from 'react';
import { Box, Button, Stack, Toolbar } from '@mui/material';
import GuestSearch from './GuestSearch';

type Mode = 'all' | 'event';

interface Props {
  mode: Mode;
  search: string;
  onSearchChange: (q: string) => void;
  onPrimaryAction: () => void;
  onAddExisting?: () => void;
}

const GuestToolbar: React.FC<Props> = ({ mode, search, onSearchChange, onPrimaryAction, onAddExisting }) => {
  const primaryLabel = 'Add Guest';
  
  return (
    <Toolbar disableGutters sx={{ mb: 2, gap: 2, flexWrap: 'wrap' }}>
      <Box sx={{ flex: 1, minWidth: 260 }}>
        <GuestSearch value={search} onChange={onSearchChange} />
      </Box>
      <Stack direction="row" spacing={1}>
        {mode === 'event' && onAddExisting && (
          <Button variant="outlined" onClick={onAddExisting}>Add Existing</Button>
        )}
        <Button variant="contained" onClick={onPrimaryAction}>
          {primaryLabel}
        </Button>
      </Stack>
    </Toolbar>
  );
};

export default GuestToolbar;
