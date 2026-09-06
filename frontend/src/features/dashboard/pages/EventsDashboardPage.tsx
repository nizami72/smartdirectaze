import React, { useEffect, useMemo, useState } from 'react';
import { Container, Grid, Box, Typography, Button, Card, CardContent, Divider } from '@mui/material';
import { Event as EventIcon, People as PeopleIcon } from '@mui/icons-material';
import StatCard from '../components/StatCard';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/api';

type DashboardData = {
  totalEvents: number;
  upcomingEvents: number;
  totalGuests: number;
  events: Array<{
    id: string;
    name: string;
    dateTime: string; // ISO
    place: string;
    guestsCount: number;
  }>;
};

const EventsDashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;
    (async () => {
      try {
        // Use shared axios client to respect base URL (e.g., /api/v1) and credentials
        const resp = await api.get('/events/dashboard', { responseType: 'json' });
        const json = resp.data;
        if (isMounted) setData(json as DashboardData);
      } catch (e: any) {
        if (isMounted) {
          // Provide a clearer message when backend returns HTML (e.g., auth page or index.html)
          const msg = typeof e?.message === 'string' && e.message.includes('Unexpected token')
            ? 'Failed to load: received non-JSON response. Please check API URL or authentication.'
            : (e?.response?.status ? `Failed to load dashboard (HTTP ${e.response.status})` : (e.message ?? 'Error'));
          setError(msg);
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    })();
    return () => {
      isMounted = false;
    };
  }, []);

  const handleCreateEvent = () => navigate('/create-event');
  const openEvent = (id: string) => navigate(`/events/${id}`);

  const hasEvents = (data?.totalEvents ?? 0) > 0;

  const content = useMemo(() => {
    if (loading) {
      return (
        <Box sx={{ py: 8, textAlign: 'center' }}>
          <Typography variant="body1" color="text.secondary">Loading...</Typography>
        </Box>
      );
    }
    if (error) {
      return (
        <Box sx={{ py: 8, textAlign: 'center' }}>
          <Typography variant="body1" color="error">{error}</Typography>
        </Box>
      );
    }
    if (!hasEvents) {
      return (
        <Box sx={{ py: 6, textAlign: 'center' }}>
          <Typography variant="h5" sx={{fontWeight: "bold"}} gutterBottom>Events Dashboard</Typography>
          <Typography variant="body1" color="text.secondary" gutterBottom>
            You don't have any events yet.
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Create your first event to get started.
          </Typography>
          <Button variant="contained" onClick={handleCreateEvent}>+ Create Event</Button>
        </Box>
      );
    }

    return (
      <>
        {/* Summary cards */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, md: 4 }}>
            <StatCard title="Total Events" value={data?.totalEvents ?? 0} icon={<EventIcon />} />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <StatCard title="Upcoming Events" value={data?.upcomingEvents ?? 0} icon={<EventIcon />} />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <StatCard title="Total Guests" value={data?.totalGuests ?? 0} icon={<PeopleIcon />} />
          </Grid>
        </Grid>

        {/* My Events */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="h6" sx={{fontWeight: "bold"}}>My Events</Typography>
        </Box>

        <Grid container spacing={2}>
          {data?.events?.map(ev => (
            <Grid key={ev.id} size={{ xs: 12 }}>
              <Card sx={{ borderRadius: 3 }}>
                <CardContent sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
                  <Box sx={{ minWidth: 0 }}>
                    <Typography variant="subtitle1" sx={{fontWeight: "bold"}} noWrap>{ev.name}</Typography>
                    <Typography variant="body2" color="text.secondary" noWrap>
                      {new Date(ev.dateTime).toLocaleDateString(undefined, { day: '2-digit', month: 'short', year: 'numeric' })}
                      {ev.place ? ` · ${ev.place}` : ''}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">Guests: {ev.guestsCount}</Typography>
                  </Box>
                  <Box sx={{ flexShrink: 0 }}>
                    <Button variant="outlined" onClick={() => openEvent(ev.id)}>Open</Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </>
    );
  }, [loading, error, data]);

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3, gap: 2 }}>
        <Box>
          <Typography variant="h4" component="h1" sx={{fontWeight: "bold"}}>Events Dashboard</Typography>
          <Typography variant="body1" color="text.secondary">Manage your events and guests.</Typography>
        </Box>
        <Button variant="contained" onClick={handleCreateEvent}>+ Create Event</Button>
      </Box>
      <Divider sx={{ mb: 3 }} />

      {content}
    </Container>
  );
};

export default EventsDashboardPage;
