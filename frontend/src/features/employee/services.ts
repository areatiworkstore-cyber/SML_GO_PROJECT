import { apiClient } from '../../services/api';
import type { Employee } from './types';

export const employeeService = {
    /**
     * Obtiene todos los usuarios (vendedores, administradores, etc.)
     */
    getEmployees(skip: number = 0, limit: number = 100): Promise<Employee[]> {
        return apiClient.get<Employee[]>('/users/', {
            params: { skip, limit }
        });
    },

    /**
     * Obtiene todos los usuarios activos (vendedores, administradores, etc.)
     */
    getEmployeesActive(): Promise<Employee[]> {
        return apiClient.get<Employee[]>('/users/active');
    },

    /**
     * Crea un nuevo usuario (vendedor, administrador, etc.)
     */
    createEmployee(employee: any): Promise<Employee> {
        return apiClient.post<Employee>('/users/', employee);
    },

    /**
     * Actualiza un usuario existente
     */
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

    /**
     * Elimina lógicamente un usuario
     */
    deleteEmployee(id: number): Promise<void> {
        return apiClient.delete<void>(`/users/${id}`);
    },

    /**
     * Restaura un usuario eliminado lógicamente
     */
    restoreEmployee(id: number): Promise<void> {
        return apiClient.patch<void>(`/users/${id}/restore`);
    },
};