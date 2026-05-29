import { apiClient } from '../../../services/api'; // Ajusta la ruta a tu apiClient según corresponda
import type { ClientScheduleResponse, ClientScheduleCreate } from '../types'; // Asegúrate de importar los tipos correctos

export const scheduleService = {
    // Obtener programaciones con filtros opcionales
    getSchedules: async (filters?: {
        user_id?: number;
        client_id?: number;
        day?: string;
        active?: boolean;
    }): Promise<ClientScheduleResponse[]> => {
        return apiClient.get<ClientScheduleResponse[]>('/client_schedules/', { params: filters });
    },

    // Crear una nueva programación
    createSchedule: async (payload: ClientScheduleCreate): Promise<ClientScheduleResponse> => {
        return apiClient.post<ClientScheduleResponse>('/client_schedules/', payload);
    },

    // Eliminar una programación
    deleteSchedule: async (id: number): Promise<{ ok: boolean; message: string }> => {
        return apiClient.delete<{ ok: boolean; message: string }>(`/client_schedules/${id}`);
    }
};