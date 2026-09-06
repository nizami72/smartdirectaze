/**
 * GuestPage
 * Purpose: Single page supporting Create / View / Edit modes for a Guest.
 * UX: Responsive layout, sections grouped into cards, uses Material UI.
 * Notes:
 * - No backend calls in MVP. Uses local state and mock data.
 * - Navigation after Save/Cancel/Delete returns to previous page (navigate(-1)).
 * - Groups: select from existing mock list; add/remove via Chips.
 * - Validation: First Name, Last Name required; basic email/phone validation.
 */
import React, { useEffect, useMemo, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardHeader,
  Container,
  Divider,
  Grid,
  Stack,
  Typography,
  Snackbar,
  Alert,
} from '@mui/material';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import GuestPersonalSection, { PersonalInfo } from '../components/GuestPersonalSection';
import GuestContactSection, { ContactInfo } from '../components/GuestContactSection';
import GuestAddressSection, { AddressInfo } from '../components/GuestAddressSection';
import GuestGroupsSection from '../components/GuestGroupsSection';
import GuestNotesSection from '../components/GuestNotesSection';
import GuestActionButtons from '../components/GuestActionButtons';
import { guestsApi } from '../api/guestsApi';

type PageMode = 'create' | 'view' | 'edit';

export interface GuestFormData {
  personal: PersonalInfo;
  contact: ContactInfo;
  address: AddressInfo;
  groups: string[];
  notes: string;
}

// Mock: available groups in business
const ALL_GROUPS = ['VIP', 'Speakers', 'Vendors', 'Friends', 'Family'];

// Mock: load guest by id (local fake)
const loadMockGuest = (guestId?: string): GuestFormData | null => {
  if (!guestId) return null;
  if (guestId === 'g1') {
    return {
      personal: { firstName: 'Alice', lastName: 'Johnson', gender: 'Female', dateOfBirth: '' },
      contact: { phone: '+1 555 111 2222', whatsapp: '+1 555 111 2222', email: 'alice@example.com' },
      address: { country: 'USA', city: 'Austin', address: '100 Main St' },
      groups: ['VIP'],
      notes: 'Allergic to peanuts.',
    };
  }
  // Default example
  return {
    personal: { firstName: 'Guest', lastName: guestId.toUpperCase(), gender: '', dateOfBirth: '' },
    contact: { phone: '', whatsapp: '', email: '' },
    address: { country: '', city: '', address: '' },
    groups: [],
    notes: '',
  };
};

const emptyForm: GuestFormData = {
  personal: { firstName: '', lastName: '', gender: '', dateOfBirth: '' },
  contact: { phone: '', whatsapp: '', email: '' },
  address: { country: '', city: '', address: '' },
  groups: [],
  notes: '',
};

const GuestPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { businessId, guestId } = useParams();

  const urlMode: PageMode = useMemo(() => {
    if (!guestId || guestId === 'new') return 'create';
    const sp = new URLSearchParams(location.search);
    return sp.get('mode') === 'edit' ? 'edit' : 'view';
  }, [guestId, location.search]);

  const [mode, setMode] = useState<PageMode>(urlMode);
  useEffect(() => setMode(urlMode), [urlMode]);

  const [form, setForm] = useState<GuestFormData>(() => (urlMode === 'create' ? emptyForm : loadMockGuest(guestId) || emptyForm));

  // Validation state
  const [errors, setErrors] = useState<{ [key: string]: string | undefined }>({});
  const [saving, setSaving] = useState(false);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' | 'info' }>({ open: false, message: '', severity: 'success' });

  const isReadOnly = mode === 'view';

  const handleChange = (patch: Partial<GuestFormData>) => {
    setForm((prev) => ({ ...prev, ...patch }));
  };

  const validate = (): boolean => {
    const e: { [key: string]: string | undefined } = {};
    if (!form.personal.firstName.trim()) e.firstName = 'First Name is required';
    if (!form.personal.lastName.trim()) e.lastName = 'Last Name is required';
    if (form.contact.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.contact.email)) e.email = 'Invalid email';
    if (form.contact.phone && !/^[+]?\d[\d\s().-]{6,}$/.test(form.contact.phone)) e.phone = 'Invalid phone';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const goBack = () => navigate(-1);

  const handleSave = async () => {
    if (!validate() || saving) return;
    try {
      setSaving(true);
      // Map frontend form to backend GuestRequest DTO
      const payload = {
        firstName: form.personal.firstName.trim(),
        lastName: form.personal.lastName.trim(),
        phone: form.contact.phone || undefined,
        whatsapp: form.contact.whatsapp || undefined,
        email: form.contact.email || undefined,
        // No event assignment and no unsupported fields here
      } as const;

      // If eventId is present in route and we later support assignment, this would be different.
      // For now, create a business-level guest without event link.
      await guestsApi.createGuest(payload as any);
      setSnackbar({ open: true, message: 'Guest created', severity: 'success' });
      // Navigate back after success notification
      navigate(-1);
    } catch (e) {
      console.error('Failed to create guest', e);
      setSnackbar({ open: true, message: 'Failed to create guest', severity: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    goBack();
  };

  const handleDelete = () => {
    console.log('Delete guest (MVP):', guestId);
    goBack();
  };

  const title = mode === 'create' ? 'Create Guest' : mode === 'edit' ? 'Edit Guest' : 'Guest';

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
        <Typography variant="h5" sx={{ fontWeight: 600 }}>{title}</Typography>
        <GuestActionButtons
          mode={mode}
          onEdit={() => setMode('edit')}
          onSave={handleSave}
          onCancel={handleCancel}
          onDelete={handleDelete}
          saving={saving}
        />
      </Stack>

      {/* Personal Information */}
      <Card variant="outlined" sx={{ mb: 2 }}>
        <CardHeader title="Personal Information" />
        <Divider />
        <CardContent>
          <GuestPersonalSection
            value={form.personal}
            onChange={(v) => handleChange({ personal: v })}
            readOnly={isReadOnly}
            errors={{ firstName: errors.firstName, lastName: errors.lastName }}
          />
        </CardContent>
      </Card>

      {/* Contact Information */}
      <Card variant="outlined" sx={{ mb: 2 }}>
        <CardHeader title="Contact Information" />
        <Divider />
        <CardContent>
          <GuestContactSection
            value={form.contact}
            onChange={(v) => handleChange({ contact: v })}
            readOnly={isReadOnly}
            errors={{ email: errors.email, phone: errors.phone }}
          />
        </CardContent>
      </Card>

      {/* Address */}
      <Card variant="outlined" sx={{ mb: 2 }}>
        <CardHeader title="Address" />
        <Divider />
        <CardContent>
          <GuestAddressSection
            value={form.address}
            onChange={(v) => handleChange({ address: v })}
            readOnly={isReadOnly}
          />
        </CardContent>
      </Card>

      {/* Groups */}
      <Card variant="outlined" sx={{ mb: 2 }}>
        <CardHeader title="Groups" />
        <Divider />
        <CardContent>
          <GuestGroupsSection
            value={form.groups}
            onChange={(v) => handleChange({ groups: v })}
            readOnly={isReadOnly}
            allGroups={ALL_GROUPS}
          />
        </CardContent>
      </Card>

      {/* Notes */}
      <Card variant="outlined" sx={{ mb: 2 }}>
        <CardHeader title="Notes" />
        <Divider />
        <CardContent>
          <GuestNotesSection value={form.notes} onChange={(v) => handleChange({ notes: v })} readOnly={isReadOnly} />
        </CardContent>
      </Card>

      {/* Bottom actions (duplicate for mobile reachability) */}
      <Box sx={{ mt: 3 }}>
        <Divider sx={{ mb: 2 }} />
        <Grid container justifyContent="flex-end">
          <GuestActionButtons
            mode={mode}
            onEdit={() => setMode('edit')}
            onSave={handleSave}
            onCancel={handleCancel}
            onDelete={handleDelete}
            saving={saving}
          />
        </Grid>
      </Box>
      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={() => setSnackbar(s => ({ ...s, open: false }))} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        <Alert onClose={() => setSnackbar(s => ({ ...s, open: false }))} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default GuestPage;
