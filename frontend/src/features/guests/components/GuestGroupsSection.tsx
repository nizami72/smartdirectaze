/**
 * GuestGroupsSection
 * Purpose: Show assigned groups as Chips and allow add/remove using Autocomplete.
 * Notes: Only selecting existing groups (provided via props.allGroups). No management.
 */
import React from 'react';
import { Autocomplete, Chip, Stack, TextField } from '@mui/material';

interface Props {
  value: string[];
  onChange: (v: string[]) => void;
  readOnly?: boolean;
  allGroups: string[];
}

const GuestGroupsSection: React.FC<Props> = ({ value, onChange, readOnly, allGroups }) => {
  const handleAdd = (_: any, newValue: string[]) => {
    onChange(newValue);
  };

  const handleDelete = (g: string) => () => {
    onChange(value.filter((x) => x !== g));
  };

  return (
    <Stack spacing={2}>
      <Autocomplete
        multiple
        options={allGroups}
        value={value}
        onChange={handleAdd}
        readOnly={!!readOnly}
        disableCloseOnSelect
        renderTags={(selected, getTagProps) =>
          selected.map((option, index) => (
            <Chip
              {...getTagProps({ index })}
              key={option}
              label={option}
              onDelete={readOnly ? undefined : handleDelete(option)}
            />
          ))
        }
        renderInput={(params) => <TextField {...params} label="Add Group" placeholder="Select group" />}
      />
    </Stack>
  );
};

export default GuestGroupsSection;
