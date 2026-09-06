import React from 'react';
import { Card, CardContent, Typography, Box } from '@mui/material';
import { StatCardProps } from '../types';

const StatCard: React.FC<StatCardProps> = ({ title, value, icon }) => {
  return (
    <Card sx={{ borderRadius: 4, boxShadow: '0 2px 10px rgba(0,0,0,0.03)' }}>
      <CardContent sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
        <Box sx={{ 
          backgroundColor: 'primary.light', 
          color: 'primary.main', 
          p: 1.5, 
          borderRadius: 3, 
          display: 'flex', 
          mr: 2,
          opacity: 0.8
        }}>
          {icon}
        </Box>
        <Box>
          <Typography variant="caption" color="text.secondary" fontWeight="medium">
            {title}
          </Typography>
          <Typography variant="h6" fontWeight="bold">
            {value}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );
};

export default StatCard;
