import api from '../../../api/api';
import { EventRequestDTO } from '../types';

export const eventsApi = {
  createEvent: async (data: EventRequestDTO) => {
    const response = await api.post('/events', data);
    return response.data;
  },
  getEvent: async (id: string) => {
    const response = await api.get(`/events/${id}`);
    return response.data;
  },
  updateEvent: async (id: string, data: EventRequestDTO) => {
    const response = await api.put(`/events/${id}`, data);
    return response.data;
  },
  deleteEvent: async (id: string) => {
    const response = await api.delete(`/events/${id}`);
    return response.data;
  },
};
