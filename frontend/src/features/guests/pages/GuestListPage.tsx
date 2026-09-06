/**
 * GuestListPage
 * Purpose: Unified Guest listing for Business and Event contexts with search and actions.
 * User interactions on this page:
 * - Search input: filter by First Name, Last Name, Phone, WhatsApp, Email.
 * - Primary action button: "Add Guest" (business) or "Create New Guest" (event) → navigates to /business/{businessId}/guests/new.
 * - Optional (event mode): "Add Existing Guest" button (no-op placeholder for MVP).
 * - Table row actions: View, Edit, Delete (business) OR Remove From Event (event).
 * - Empty state button: "Add First Guest" → navigates to create route.
 */
import React, { useEffect, useMemo, useState } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  List,
  ListItemButton,
  Checkbox,
  ListItemText,
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import GuestToolbar from '../components/GuestToolbar';
import GuestTable, { Guest } from '../components/GuestTable';
import EmptyGuestState from '../components/EmptyGuestState';
import { guestsApi, GuestResponse } from '../api/guestsApi';

type Mode = 'all' | 'event';

const GuestListPage: React.FC = () => {
  const navigate = useNavigate();
  const { businessId, eventId } = useParams();
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [items, setItems] = useState<Guest[]>([]);
  const [allGuests, setAllGuests] = useState<Guest[]>([]);
  const [selectOpen, setSelectOpen] = useState(false);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);

  const mode: Mode = eventId ? 'event' : 'all';

  useEffect(() => {
    let active = true;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        let data: GuestResponse[] = [];
        if (mode === 'event' && eventId) {
          data = await guestsApi.listByEvent(eventId);
        } else {
          data = await guestsApi.listAll();
        }
        if (!active) return;
        const mapped: Guest[] = data.map((g) => ({
          id: g.id,
          firstName: g.firstName,
          lastName: g.lastName,
          phone: g.phone ?? undefined,
          whatsapp: g.whatsapp ?? undefined,
          email: g.email ?? undefined,
        }));
        setItems(mapped);
      } catch (err: any) {
        if (!active) return;
        setError(err?.message ?? 'Failed to load guests');
      } finally {
        if (active) setLoading(false);
      }
    }
    load();
    return () => {
      active = false;
    };
  }, [mode, eventId]);

  const guests = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return items;
    return items.filter((g) => {
      const hay = [g.firstName, g.lastName, g.phone, g.whatsapp, g.email]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return hay.includes(q);
    });
  }, [items, query]);

  const title = mode === 'event' ? 'Event Guests' : 'Guests';

  const openSelectDialog = async () => {
    try {
      const data = await guestsApi.listAll();
      const mapped: Guest[] = data.map((g) => ({
        id: g.id,
        firstName: g.firstName,
        lastName: g.lastName,
        phone: g.phone ?? undefined,
        whatsapp: g.whatsapp ?? undefined,
        email: g.email ?? undefined,
      }));
      setAllGuests(mapped);
      setSelectedIds([]);
      setSelectOpen(true);
    } catch (e: any) {
      setError(e?.message || 'Failed to load guests');
    }
  };

  const confirmAddToEvent = async () => {
    if (!eventId) return;
    try {
      await Promise.all(selectedIds.map((id) => guestsApi.addToEvent(eventId, id)));
      setSelectOpen(false);
      // refresh event guest list
      const refreshed = await guestsApi.listByEvent(eventId);
      const mapped: Guest[] = refreshed.map((g) => ({
        id: g.id,
        firstName: g.firstName,
        lastName: g.lastName,
        phone: g.phone ?? undefined,
        whatsapp: g.whatsapp ?? undefined,
        email: g.email ?? undefined,
      }));
      setItems(mapped);
    } catch (e: any) {
      setError(e?.message || 'Failed to add guest(s) to event');
    }
  };

  const handleAddPrimary = () => {
    if (mode === 'event') {
      // In event mode, Add Guest opens selection dialog of all existing guests
      openSelectDialog();
    } else {
      // In all-guests mode navigate to create page
      navigate('/guest/new');
    }
  };

  const handleView = (id: string) => {
    if (!businessId) return;
    navigate(`/business/${businessId}/guests/${id}`);
  };

  const handleEdit = (id: string) => {
    if (!businessId) return;
    navigate(`/business/${businessId}/guests/${id}?mode=edit`);
  };

  const handleDelete = async (id: string) => {
    if (mode !== 'all') return;
    try {
      await guestsApi.deleteGuest(id);
      setItems((prev) => prev.filter((g) => g.id !== id));
    } catch (e: any) {
      setError(e?.message || 'Failed to delete guest');
    }
  };

  const handleRemoveFromEvent = async (id: string) => {
    if (!eventId) return;
    try {
      await guestsApi.removeFromEvent(eventId, id);
      setItems((prev) => prev.filter((g) => g.id !== id));
    } catch (e: any) {
      setError(e?.message || 'Failed to remove guest from event');
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h5" sx={{ fontWeight: 600 }}>
          {title} ({guests.length})
        </Typography>
      </Box>

      <Paper elevation={0} sx={{ p: 2 }}>
        <GuestToolbar
          mode={mode}
          search={query}
          onSearchChange={setQuery}
          onPrimaryAction={handleAddPrimary}
          onAddExisting={mode === 'event' ? openSelectDialog : undefined}
        />

        {loading ? (
          <Box sx={{ py: 6, textAlign: 'center', color: 'text.secondary' }}>Loading guests…</Box>
        ) : error ? (
          <Box sx={{ py: 6, textAlign: 'center', color: 'error.main' }}>{error}</Box>
        ) : guests.length === 0 ? (
          <EmptyGuestState mode={mode} onAdd={handleAddPrimary} />
        ) : (
          <GuestTable
            mode={mode}
            rows={guests}
            onView={handleView}
            onEdit={handleEdit}
            onDelete={handleDelete}
            onRemoveFromEvent={mode === 'event' ? handleRemoveFromEvent : undefined}
          />
        )}
      </Paper>

      {/* Add Existing Guests to Event Dialog */}
      <Dialog open={selectOpen} onClose={() => setSelectOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Select guests to add</DialogTitle>
        <DialogContent dividers>
          <List dense>
            {allGuests.map((g) => {
              const label = `${g.firstName} ${g.lastName}`.trim();
              const checked = selectedIds.includes(g.id);
              return (
                <ListItemButton
                  key={g.id}
                  role={undefined}
                  onClick={() =>
                    setSelectedIds((prev) =>
                      checked ? prev.filter((id) => id !== g.id) : [...prev, g.id]
                    )
                  }
                >
                  <Checkbox edge="start" checked={checked} tabIndex={-1} disableRipple />
                  <ListItemText primary={label} secondary={[g.phone, g.email].filter(Boolean).join(' • ')} />
                </ListItemButton>
              );
            })}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSelectOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={confirmAddToEvent} disabled={!selectedIds.length}>
            Add {selectedIds.length ? `(${selectedIds.length})` : ''}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default GuestListPage;
