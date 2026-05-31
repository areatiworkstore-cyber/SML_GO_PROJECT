import { apiClient } from '../../services/api';
import type { Client, ClientCreate, BusinessType, ClientGroup } from './types';

export const clientService = {
  getClients(userId?: number): Promise<Client[]> {
    const params = userId ? { user_id: userId } : undefined;
    return apiClient.get<Client[]>('/clients', { params });
  },

  getClient(id: number): Promise<Client> {
    return apiClient.get<Client>(`/clients/${id}`);
  },

  createClient(client: ClientCreate): Promise<Client> {
    return apiClient.post<Client>('/clients', client);
  },

  updateClient(id: number, client: Partial<ClientCreate>): Promise<Client> {
    return apiClient.put<Client>(`/clients/${id}`, client);
  },

  getMapsRedirect(id: number): Promise<{ url: string }> {
    return apiClient.get<{ url: string }>(`/clients/${id}/maps-redirect`);
  },

  getNextClientCode(): Promise<{ next_code: string }> {
    return apiClient.get<{ next_code: string }>('/clients/next-code');
  },
};

export const masterDataService = {
  getBusinessTypes(): Promise<BusinessType[]> {
    return apiClient.get<BusinessType[]>('/master_data/business-types');
  },

  getClientGroups(): Promise<ClientGroup[]> {
    return apiClient.get<ClientGroup[]>('/master_data/client-groups');
  }
};
