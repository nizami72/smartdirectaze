import React from 'react';
import {Box, Divider, FormControl, Grid, InputLabel, MenuItem, Select, TextField, Typography,} from '@mui/material';
import {EventFormData} from '../types';

interface Props {
    formData: EventFormData;
    errors: Partial<Record<keyof EventFormData, string>>;
    onChange: (e: React.ChangeEvent<any>) => void;
    disabled?: boolean;
}

const EventGeneralInformation: React.FC<Props> = ({formData, errors, onChange, disabled}) => {
    return (
        <Box sx={{mb: 4}}>
            <Typography variant="h6" gutterBottom sx={{fontWeight: 600}}>
                General Information
            </Typography>
            <Divider sx={{mb: 3}}/>
            <Grid container spacing={3}>
                <Grid size={{xs: 12, md: 6}}>
                    <TextField
                        fullWidth
                        label="Event Name"
                        name="name"
                        value={formData.name}
                        onChange={onChange}
                        error={!!errors.name}
                        helperText={errors.name}
                        required
                        disabled={disabled}
                    />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <FormControl fullWidth disabled={disabled}>
                        <InputLabel id="event-type-label">Event Type</InputLabel>
                        <Select
                            labelId="event-type-label"
                            name="type"
                            value={formData.type}
                            label="Event Type"
                            onChange={(e) => onChange(e as unknown as React.ChangeEvent<any>)}
                        >
                            <MenuItem value="Party">Party</MenuItem>
                            <MenuItem value="Wedding">Wedding</MenuItem>
                            <MenuItem value="Birthday">Birthday</MenuItem>
                            <MenuItem value="Conference">Conference</MenuItem>
                            <MenuItem value="Workshop">Workshop</MenuItem>
                            <MenuItem value="Other">Other</MenuItem>
                        </Select>
                    </FormControl>
                </Grid>
                <Grid size={12}>
                    <TextField fullWidth label="Description" name="description" value={formData.description}
                               onChange={onChange} multiline rows={3} disabled={disabled}/>
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <TextField
                        fullWidth
                        label="Start Date"
                        name="startDate"
                        type="datetime-local"
                        value={formData.startDate}
                        onChange={onChange as React.ChangeEventHandler<HTMLInputElement>}
                        slotProps={{
                            inputLabel: {shrink: true},
                        }}
                        error={!!errors.startDate}
                        helperText={errors.startDate}
                        required
                        disabled={disabled}
                    />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <TextField
                        fullWidth
                        label="End Date"
                        name="endDate"
                        type="datetime-local"
                        value={formData.endDate}
                        onChange={onChange as React.ChangeEventHandler<HTMLInputElement>}
                        slotProps={{
                            inputLabel: {
                                shrink: true,
                            },
                        }}
                        disabled={disabled}
                    />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <TextField fullWidth label="Venue" name="venue" value={formData.venue} onChange={onChange}
                               disabled={disabled}/>
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <TextField fullWidth label="Maximum Guests" name="maxGuests" type="number"
                               value={formData.maxGuests} onChange={onChange} disabled={disabled}/>
                </Grid>
            </Grid>
        </Box>
    );
};

export default EventGeneralInformation;
