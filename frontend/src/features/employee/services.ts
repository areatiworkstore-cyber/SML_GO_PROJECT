import { apiClient } from '../../services/api';
import type { Employee } from './types';

export const employeeService = {
    getEmployees(skip: number = 0, limit: number = 100): Promise<Employee[]> {
        return apiClient.get<Employee[]>('/users/', {
            params: { skip, limit }
        });
    },

    // Puedes dejar mapeados los demás de una vez si el backend los soporta:
    createEmployee(employee: any): Promise<Employee> {
        return apiClient.post<Employee>('/users/', employee);
    },

    // O los endpoints que correspondan según tu backend (por ejemplo /users/{id})
};