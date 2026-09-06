import React from 'react';
import {
    Box,
    Divider,
    FormControl,
    FormControlLabel,
    FormLabel,
    Grid,
    InputLabel,
    MenuItem,
    Radio,
    RadioGroup,
    Select,
    TextField,
    Typography
} from '@mui/material';
import {EventFormData, EventStatus, EventVisibility} from '../types';

interface GeneralInformationSectionProps {
    formData: EventFormData;
    errors: Partial<Record<keyof EventFormData, string>>;
    onChange: (e: React.ChangeEvent<any>) => void;
}

const GeneralInformationSection: React.FC<GeneralInformationSectionProps> = ({
                                                                                 formData,
                                                                                 errors,
                                                                                 onChange
                                                                             }) => {
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
                    />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <FormControl fullWidth>
                        <InputLabel id="event-type-label">Event Type</InputLabel>
                        <Select
                            labelId="event-type-label"
                            name="type"
                            value={formData.type}
                            label="Event Type"
                            onChange={(e) => onChange(e as unknown as React.ChangeEvent<HTMLInputElement>)}
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
                    <TextField
                        fullWidth
                        label="Description"
                        name="description"
                        value={formData.description}
                        onChange={onChange}
                        multiline
                        rows={3}
                    />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <TextField
                        fullWidth
                        label="Start Date"
                        name="startDate"
                        type="datetime-local"
                        value={formData.startDate}
                        // 1. Явно приводим тип обработчика события к ожидаемому MUI
                        onChange={onChange as React.ChangeEventHandler<HTMLInputElement>}
                        error={!!errors.startDate}
                        helperText={errors.startDate}
                        required
                        // 2. Используем современный slotProps вместо устаревшего InputLabelProps
                        slotProps={{
                            inputLabel: {shrink: true}
                        }}
                    />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <TextField
                        fullWidth
                        label="End Date"
                        name="endDate"
                        type="datetime-local"
                        value={formData.endDate}
                        onChange={onChange as (e: React.ChangeEvent<HTMLInputElement>) => void}
                        slotProps={{
                            inputLabel: {shrink: true},
                        }}
                    />

                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <TextField
                        fullWidth
                        label="Venue"
                        name="venue"
                        value={formData.venue}
                        onChange={onChange}
                    />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <TextField
                        fullWidth
                        label="Maximum Guests"
                        name="maxGuests"
                        type="number"
                        value={formData.maxGuests}
                        onChange={onChange}
                    />
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <FormControl component="fieldset">
                        <FormLabel component="legend">Visibility</FormLabel>
                        <RadioGroup
                            row
                            name="visibility"
                            value={formData.visibility}
                            onChange={onChange}
                        >
                            <FormControlLabel value={EventVisibility.PUBLIC} control={<Radio/>} label="Public"/>
                            <FormControlLabel value={EventVisibility.PRIVATE} control={<Radio/>} label="Private"/>
                        </RadioGroup>
                    </FormControl>
                </Grid>
                <Grid size={{xs: 12, md: 6}}>
                    <FormControl component="fieldset">
                        <FormLabel component="legend">Status</FormLabel>
                        <RadioGroup
                            row
                            name="status"
                            value={formData.status}
                            onChange={onChange}
                        >
                            <FormControlLabel value={EventStatus.DRAFT} control={<Radio/>} label="Draft"/>
                            <FormControlLabel value={EventStatus.PUBLISHED} control={<Radio/>} label="Published"/>
                        </RadioGroup>
                    </FormControl>
                </Grid>
            </Grid>
        </Box>
    );
};

export default GeneralInformationSection;
