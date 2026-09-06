import React from 'react';
import { Box, Typography, Divider, Grid, Paper, Stack, Button } from '@mui/material';

interface GuestStats {
  total: number;
  sent: number;
  confirmed: number;
  pending: number;
  declined: number;
}

interface Props {
  stats: GuestStats;
  onViewGuests: () => void;
  onSendInvitations: () => void;
}

const StatCard: React.FC<{ label: string; value: number }> = ({ label, value }) => (
  <Paper variant="outlined" sx={{ p: 2, borderRadius: 2 }}>
    <Typography variant="h6" sx={{ fontWeight: 700 }}>
      {value}
    </Typography>
    <Typography variant="body2" color="text.secondary">
      {label}
    </Typography>
  </Paper>
);

const EventGuestSummary: React.FC<Props> = ({ stats, onViewGuests, onSendInvitations }) => {
  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
        Guests Summary
      </Typography>
      <Divider sx={{ mb: 3 }} />
      <Grid container spacing={2} columns={{ xs: 12, md: 5 }}>
        <Grid size={{ xs: 6, md: 1 }}>
          <StatCard label="Total Guests" value={stats.total} />
        </Grid>
        <Grid size={{ xs: 6, md: 1 }}>
          <StatCard label="Invitations Sent" value={stats.sent} />
        </Grid>
        <Grid size={{ xs: 6, md: 1 }}>
          <StatCard label="Confirmed" value={stats.confirmed} />
        </Grid>
        <Grid size={{ xs: 6, md: 1 }}>
          <StatCard label="Pending" value={stats.pending} />
        </Grid>
        <Grid size={{ xs: 6, md: 1 }}>
          <StatCard label="Declined" value={stats.declined} />
        </Grid>
      </Grid>
      <Stack direction="row" spacing={2} sx={{ mt: 3 }}>
        <Button variant="outlined" onClick={onViewGuests}>View Guests</Button>
        <Button variant="contained" color="primary" onClick={onSendInvitations}>Send Invitations</Button>
      </Stack>
    </Box>
  );
};

export default EventGuestSummary;
