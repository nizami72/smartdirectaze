import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api.ts';

import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Container,
  // Grid,
  Stack,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';

const MyBusinessesPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [summaries, setSummaries] = useState([]);

  useEffect(() => {
    let isMounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        // Load per-industry summaries for current user
        const res = await api.get('/api/v1/businesses/my');
        const items = (res.data || []).map((b) => {
          const display = b.displayName || (b.industry === 'SHOP' ? 'Shop' : b.industry === 'EVENTS' ? 'Events' : b.industry === 'DENTAL' ? 'Dental' : (b.industry || 'Business'));
          return {
            industry: b.industry,
            displayName: display,
            count: b.count ?? 0,
            raw: b,
          };
        });
        if (isMounted) setSummaries(items);
      } catch (e) {
        console.error('Failed to load businesses', e);
        if (isMounted) setError('Failed to load your businesses. Please try again.');
      } finally {
        if (isMounted) setLoading(false);
      }
    };
    load();
    return () => {
      isMounted = false;
    };
  }, []);

  const handleCreateBusiness = () => {
    navigate('/choose-business');
  };

  const handleOpen = (summary) => {
    // Route to existing feature pages where available. For MVP, use unified dashboard.
    navigate('/dashboard');
  };

  const hasBusinesses = summaries.length > 0;

  const pluralized = (count, industry) => {
    const word = industry === 'EVENTS' ? 'event' : industry === 'SHOP' ? 'shop' : 'item';
    const suffix = count === 1 ? '' : 's';
    return `${count} ${word}${suffix}`;
  };

  return (
    <Container maxWidth="lg" sx={{ py: { xs: 3, md: 6 } }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 3 }}>
        <Typography variant="h4" fontWeight={700} gutterBottom sx={{ mb: 0 }}>
          My Businesses
        </Typography>

        {hasBusinesses && (
          <Button variant="text" color="primary" onClick={handleCreateBusiness}>
            + Add Business
          </Button>
        )}
      </Stack>

      {/* Empty state */}
      {!loading && !error && !hasBusinesses && (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            textAlign: 'center',
            py: 8,
            px: 2,
          }}
        >
          <Typography variant="h5" fontWeight={700} gutterBottom>
            You don't have any businesses yet.
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Create your first business to start using the platform.
          </Typography>
          <Button variant="contained" color="primary" size="large" onClick={handleCreateBusiness}>
            Create Business
          </Button>
        </Box>
      )}

      {/* Error state */}
      {!loading && error && (
        <Box sx={{ py: 4 }}>
          <Typography color="error" align="center">{error}</Typography>
          <Stack direction="row" justifyContent="center" sx={{ mt: 2 }}>
            <Button onClick={() => window.location.reload()}>Retry</Button>
          </Stack>
        </Box>
      )}

      {/* List state */}
      {!loading && !error && hasBusinesses && (
        <Grid container spacing={3}>
          {summaries.map((s) => (
            <Grid key={s.industry} size={{ xs: 12, sm: 6, md: 4 }}>
              <Card elevation={2} sx={{ height: '100%' }}>
                <CardContent>
                  <Typography variant="h6" fontWeight={700} gutterBottom>
                    {s.displayName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {pluralized(s.count, s.industry)}
                  </Typography>
                </CardContent>
                <CardActions sx={{ px: 2, pb: 2 }}>
                  <Button variant="contained" onClick={() => handleOpen(s)}>
                    Open
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Container>
  );
};

export default MyBusinessesPage;
