import React from 'react';
import { Box, Button, Container, Paper, Typography } from '@mui/material';
import { useNavigate, useParams, useLocation } from 'react-router-dom';

/**
 * GuestPagePlaceholder
 * Purpose: Temporary placeholder page until full GuestPage (Create/View/Edit) is implemented.
 * User interactions on this page:
 * - "Back to Guests" button: returns user to the corresponding Guest List page for the current business (and event if applicable).
 */
const GuestPagePlaceholder = ({ mode = 'view' }) => {
  const navigate = useNavigate();
  const { businessId, eventId, guestId } = useParams();
  const location = useLocation();

  const handleBack = () => {
    if (eventId) {
      navigate(`/business/${businessId}/events/${eventId}/guests`);
    } else {
      navigate(`/business/${businessId}/guests`);
    }
  };

  return (
    <Container maxWidth="sm" sx={{ py: 6 }}>
      <Paper sx={{ p: 4 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Typography variant="h5" sx={{ fontWeight: 600 }}>
            Guest Page Placeholder
          </Typography>
          <Typography color="text.secondary">
            This route is intentionally not implemented in the MVP.
          </Typography>
          <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
            {`Mode: ${mode}\nBusiness: ${businessId || '-'}\nEvent: ${eventId || '-'}\nGuest: ${guestId || '-'}\nPathname: ${location.pathname}`}
          </Typography>
          <Box>
            <Button variant="contained" onClick={handleBack}>
              Back to Guests
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default GuestPagePlaceholder;
