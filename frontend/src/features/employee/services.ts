import { apiClient } from '../../services/api';
import type { Employee } from './types';

export const employeeService = {
    getEmployees(skip: number = 0, limit: number = 100): Promise<Employee[]> {
        return apiClient.get<Employee[]>('/users/', {
            params: { skip, limit }
        });
    },

    createEmployee(employee: any): Promise<Employee> {
        return apiClient.post<Employee>('/users/', employee);
    },

    updateEmployee(id: number, employee: any): Promise<Employee> {
        return apiClient.put<Employee>(`/users/${id}`, employee);
    },

    /**
     * Obtiene la cartera de clientes asignada de manera exclusiva a un asesor
     */
    getClientsByAdvisor(userId: number): Promise<any[]> {
        // Usamos apiClient para mantener los interceptores de token, cabeceras y consistencia
        return apiClient.get<any[]>('/clients/', {
            params: { user_id: userId }
        });
    },

    getRoleUsers(skip: number = 0, limit: number = 100): Promise<any[]> {
        return apiClient.get<any[]>('/users/role_users', { params: { skip, limit } });
    }
};