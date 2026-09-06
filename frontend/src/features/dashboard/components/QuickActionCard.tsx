import React from 'react';
import { Card, CardContent, Typography, Button, Box } from '@mui/material';
import { QuickActionProps } from '../types';

const QuickActionCard: React.FC<QuickActionProps> = ({ title, description, icon, buttonText, onClick }) => {
  return (
    <Card sx={{ height: '100%', borderRadius: 4, boxShadow: '0 4px 20px rgba(0,0,0,0.05)' }}>
      <CardContent sx={{ p: 3, display: 'flex', flexDirection: 'column', height: '100%' }}>
        <Box sx={{ color: 'primary.main', mb: 2 }}>
          {icon}
        </Box>
        <Typography
            variant="h5"
            component="h2"
            gutterBottom
            sx={{ fontWeight: 'bold' }}
        >
          {title}
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 3, flexGrow: 1 }}>
          {description}
        </Typography>
        <Button variant="contained" onClick={onClick} sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 'bold' }}>
          {buttonText}
        </Button>
      </CardContent>
    </Card>
  );
};

export default QuickActionCard;
