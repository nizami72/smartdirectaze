export enum EventVisibility {
  PRIVATE = 'PRIVATE',
  PUBLIC = 'PUBLIC'
}

export enum EventStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED'
}

export interface EventFormData {
  name: string;
  type: string;
  description: string;
  startDate: string; // ISO string
  endDate: string; // ISO string
  venue: string;
  maxGuests: number | '';
  visibility: EventVisibility;
  status: EventStatus;
  organizerName: string;
  organizerPhone: string;
  organizerEmail: string;
  notes: string;
}

export interface EventRequestDTO {
  name: string;
  dateTime: string; // ISO string, mapping to Backend's LocalDateTime
  place: string;
  description: string;
  // Note: Backend EventRequest currently only supports these 4 fields.
  // Other fields from EventFormData will be marked as TODO for API integration.
}
