import React from 'react';
import { Box, Typography } from '@mui/material';

const WelcomeSection: React.FC = () => {
  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
        Welcome back!
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Manage your guests and events from one place.
      </Typography>
    </Box>
  );
};

export default WelcomeSection;
