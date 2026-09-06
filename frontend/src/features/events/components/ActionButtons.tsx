import React from 'react';
import { Box, Button, CircularProgress } from '@mui/material';
import { useNavigate } from 'react-router-dom';

interface ActionButtonsProps {
  isSubmitting: boolean;
  onSaveDraft: () => void;
}

const ActionButtons: React.FC<ActionButtonsProps> = ({ isSubmitting, onSaveDraft }) => {
  const navigate = useNavigate();

  return (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 4 }}>
      <Button 
        variant="outlined" 
        onClick={() => navigate(-1)}
        disabled={isSubmitting}
      >
        Cancel
      </Button>
      <Button 
        variant="outlined" 
        color="secondary"
        onClick={onSaveDraft}
        disabled={isSubmitting}
      >
        Save Draft
      </Button>
      <Button 
        type="submit" 
        variant="contained" 
        color="primary"
        disabled={isSubmitting}
        sx={{ minWidth: 140 }}
      >
        {isSubmitting ? <CircularProgress size={24} color="inherit" /> : 'Create Event'}
      </Button>
    </Box>
  );
};

export default ActionButtons;
