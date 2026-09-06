/**
 * GuestTable
 * Purpose: Responsive Material UI table for displaying guests with action buttons.
 * User interactions in this component:
 * - Per-row action buttons (rendered via GuestActions): View, Edit, Delete (business) or Remove From Event (event).
 */
import React from 'react';
import {
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import GuestActions from './GuestActions';

export interface Guest {
  id: string;
  firstName: string;
  lastName: string;
  phone?: string;
  whatsapp?: string;
  email?: string;
  groups?: string[];
  assignedToEvent?: boolean;
}

type Mode = 'all' | 'event';

interface Props {
  mode: Mode;
  rows: Guest[];
  onView: (id: string) => void;
  onEdit: (id: string) => void;
  onDelete: (id: string) => void;
  onRemoveFromEvent?: (id: string) => void;
}

const GuestTable: React.FC<Props> = ({ mode, rows, onView, onEdit, onDelete, onRemoveFromEvent }) => {
  return (
    <TableContainer>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>First Name</TableCell>
            <TableCell>Last Name</TableCell>
            <TableCell>Phone</TableCell>
            <TableCell>WhatsApp</TableCell>
            <TableCell>Email</TableCell>
            <TableCell>Groups</TableCell>
            <TableCell align="right">Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((g) => (
            <TableRow key={g.id} hover>
              <TableCell>{g.firstName}</TableCell>
              <TableCell>{g.lastName}</TableCell>
              <TableCell>{g.phone ?? '—'}</TableCell>
              <TableCell>{g.whatsapp ?? '—'}</TableCell>
              <TableCell>{g.email ?? '—'}</TableCell>
              <TableCell>
                {g.groups?.length ? (
                  g.groups.map((gr) => (
                    <Chip key={gr} size="small" label={gr} sx={{ mr: 0.5, mb: 0.5 }} />
                  ))
                ) : (
                  '—'
                )}
              </TableCell>
              <TableCell align="right">
                <GuestActions
                  mode={mode}
                  onView={() => onView(g.id)}
                  onEdit={() => onEdit(g.id)}
                  onDelete={() => onDelete(g.id)}
                  onRemoveFromEvent={onRemoveFromEvent ? () => onRemoveFromEvent(g.id) : undefined}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default GuestTable;
