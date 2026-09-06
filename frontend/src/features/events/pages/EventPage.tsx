import React, { useEffect, useMemo, useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  Snackbar,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import EventGeneralInformation from '../components/EventGeneralInformation';
import EventStatusSection from '../components/EventStatusSection';
import EventOrganizerSection from '../components/EventOrganizerSection';
import EventGuestSummary from '../components/EventGuestSummary';
import EventNotesSection from '../components/EventNotesSection';
import EventActionButtons from '../components/EventActionButtons';
import { EventFormData, EventRequestDTO, EventStatus, EventVisibility } from '../types';
import { eventsApi } from '../api/eventsApi';

type PageMode = 'create' | 'view' | 'edit';

const initialFormData: EventFormData = {
  name: '',
  type: '',
  description: '',
  startDate: '',
  endDate: '',
  venue: '',
  maxGuests: '',
  visibility: EventVisibility.PUBLIC,
  status: EventStatus.DRAFT,
  organizerName: '',
  organizerPhone: '',
  organizerEmail: '',
  notes: '',
};

const EventPage: React.FC = () => {
  const { eventId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const isCreateRoute = location.pathname.endsWith('/create-event');
  const [mode, setMode] = useState<PageMode>(isCreateRoute ? 'create' : 'view');

  const [formData, setFormData] = useState<EventFormData>(initialFormData);
  const [errors, setErrors] = useState<Partial<Record<keyof EventFormData, string>>>({});
  const [loading, setLoading] = useState<boolean>(!isCreateRoute);
  const [saving, setSaving] = useState<boolean>(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'info';
  }>({ open: false, message: '', severity: 'success' });

  // Guests summary (placeholder for now)
  const [guestStats, setGuestStats] = useState({
    total: 0,
    sent: 0,
    confirmed: 0,
    pending: 0,
    declined: 0,
  });

  useEffect(() => {
    let isMounted = true;
    if (!isCreateRoute && eventId) {
      (async () => {
        try {
          const data = await eventsApi.getEvent(eventId);
          if (!isMounted) return;
          // Map backend event to form fields as best as possible
          setFormData({
            name: data?.name ?? '',
            type: data?.type ?? '',
            description: data?.description ?? '',
            startDate: data?.dateTime ?? '',
            endDate: data?.endDate ?? '',
            venue: data?.place ?? '',
            maxGuests: data?.maxGuests ?? '',
            visibility: data?.visibility ?? EventVisibility.PUBLIC,
            status: data?.status ?? EventStatus.DRAFT,
            organizerName: data?.organizerName ?? '',
            organizerPhone: data?.organizerPhone ?? '',
            organizerEmail: data?.organizerEmail ?? '',
            notes: data?.notes ?? '',
          });
          // Guest stats if backend provides
          if (data?.guestStats) {
            setGuestStats({
              total: data.guestStats.total ?? 0,
              sent: data.guestStats.sent ?? 0,
              confirmed: data.guestStats.confirmed ?? 0,
              pending: data.guestStats.pending ?? 0,
              declined: data.guestStats.declined ?? 0,
            });
          }
        } catch (e) {
          console.error('Failed to load event', e);
          setSnackbar({ open: true, message: 'Failed to load event', severity: 'error' });
        } finally {
          if (isMounted) setLoading(false);
        }
      })();
    } else {
      setLoading(false);
    }
    return () => {
      isMounted = false;
    };
  }, [eventId, isCreateRoute]);

  const disabled = useMemo(() => mode === 'view', [mode]);

  const handleChange = (e: React.ChangeEvent<any>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name as keyof EventFormData]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const validateForm = () => {
    const newErrors: Partial<Record<keyof EventFormData, string>> = {};
    if (!formData.name.trim()) newErrors.name = 'Event Name is required';
    if (!formData.startDate) newErrors.startDate = 'Start Date is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = async () => {
    if (!validateForm()) return;
    setSaving(true);
    try {
      const request: EventRequestDTO = {
        name: formData.name,
        dateTime: formData.startDate,
        place: formData.venue || 'TBD',
        description: formData.description,
      };
      if (mode === 'create') {
        const created = await eventsApi.createEvent(request);
        setSnackbar({ open: true, message: 'Event created', severity: 'success' });
        // Navigate to created event view if id is returned
        const id = created?.id ?? created?.eventId;
        if (id) {
          navigate(`/events/${id}`);
          setMode('view');
        } else {
          navigate('/dashboard');
        }
      } else if (eventId) {
        await eventsApi.updateEvent(eventId, request);
        setSnackbar({ open: true, message: 'Event updated', severity: 'success' });
        setMode('view');
      }
    } catch (e) {
      console.error('Save failed', e);
      setSnackbar({ open: true, message: 'Failed to save event', severity: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!eventId) return;
    try {
      await eventsApi.deleteEvent(eventId);
      setSnackbar({ open: true, message: 'Event deleted', severity: 'success' });
      setTimeout(() => navigate('/dashboard'), 800);
    } catch (e) {
      console.error('Delete failed', e);
      setSnackbar({ open: true, message: 'Failed to delete event', severity: 'error' });
    }
  };

  const handleCancel = () => {
    if (mode === 'create') {
      navigate(-1);
    } else if (mode === 'edit') {
      setMode('view');
    }
  };

  const handleEdit = () => setMode('edit');

  const handleViewGuests = () => {
    if (eventId) navigate(`/events/${eventId}/guests`);
  };

  const handleSendInvitations = () => {
    setSnackbar({ open: true, message: 'Invitation sending is not implemented in MVP', severity: 'info' });
  };

  const handleCloseSnackbar = () => setSnackbar((s) => ({ ...s, open: false }));

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ py: 6, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 6 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 700 }}>
          {mode === 'create' ? 'Create Event' : formData.name || 'Event'}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {mode === 'create'
            ? 'Fill in the details below to create your new event.'
            : mode === 'view'
            ? 'View event details.'
            : 'Edit event details.'}
        </Typography>
      </Box>

      <Paper elevation={0} sx={{ p: { xs: 3, md: 5 }, borderRadius: 3, border: '1px solid', borderColor: 'divider' }}>
        <EventGeneralInformation formData={formData} errors={errors} onChange={handleChange} disabled={disabled} />
        <EventStatusSection formData={formData} onChange={handleChange} disabled={disabled} />
        <EventOrganizerSection formData={formData} onChange={handleChange} disabled={disabled} />
        <EventGuestSummary stats={guestStats} onViewGuests={handleViewGuests} onSendInvitations={handleSendInvitations} />
        <EventNotesSection formData={formData} onChange={handleChange} disabled={disabled} />

        <EventActionButtons
          mode={mode}
          isSubmitting={saving}
          onSave={handleSave}
          onCancel={handleCancel}
          onDelete={handleDelete}
          onEdit={handleEdit}
          onViewGuests={handleViewGuests}
          onSendInvitations={handleSendInvitations}
        />
      </Paper>

      <Snackbar open={snackbar.open} autoHideDuration={4000} onClose={handleCloseSnackbar} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default EventPage;
