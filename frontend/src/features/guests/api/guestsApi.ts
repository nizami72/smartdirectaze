import api from '../../../api/api';

export interface GuestCreateRequest {
  salutation?: string | null;
  firstName: string;
  lastName: string;
  phone?: string | null;
  whatsapp?: string | null;
  email?: string | null;
  language?: string | null;
  status?: string | null;
}

export interface GuestResponse {
  id: string;
  salutation?: string | null;
  firstName: string;
  lastName: string;
  phone?: string | null;
  whatsapp?: string | null;
  email?: string | null;
  language?: string | null;
  status?: string | null;
}

export const guestsApi = {
  createGuest: async (data: GuestCreateRequest) => {
    const response = await api.post('/guests', data);
    return response.data;
  },
  listAll: async (): Promise<GuestResponse[]> => {
    const response = await api.get('/guests');
    return response.data;
  },
  listByEvent: async (eventId: string): Promise<GuestResponse[]> => {
    const response = await api.get(`/events/${eventId}/guests`);
    return response.data;
  },
  addToEvent: async (eventId: string, guestId: string): Promise<void> => {
    await api.post(`/events/${eventId}/guests/${guestId}`);
  },
  removeFromEvent: async (eventId: string, guestId: string): Promise<void> => {
    await api.delete(`/events/${eventId}/guests/${guestId}`);
  },
  deleteGuest: async (guestId: string): Promise<void> => {
    await api.delete(`/guests/${guestId}`);
  },
};
