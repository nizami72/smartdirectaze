import React from 'react';
import { Box, Typography, Button, Paper } from '@mui/material';
import { EmptyStateProps } from '../types';

const EmptyStateSection: React.FC<EmptyStateProps & { sectionTitle: string }> = ({ 
  sectionTitle, 
  title, 
  description, 
  buttonText, 
  onClick 
}) => {
  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" gutterBottom  sx={{ mb: 2, fontWeight: "bold" }}>
        {sectionTitle}
      </Typography>
      <Paper 
        variant="outlined" 
        sx={{ 
          p: 6, 
          textAlign: 'center', 
          borderRadius: 4, 
          backgroundColor: 'transparent',
          borderStyle: 'dashed',
          borderWidth: 2
        }}
      >
        <Typography variant="h6" color="text.secondary" gutterBottom>
          {title}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          {description}
        </Typography>
        <Button 
          variant="outlined" 
          onClick={onClick}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 'bold' }}
        >
          {buttonText}
        </Button>
      </Paper>
    </Box>
  );
};

export default EmptyStateSection;
