import { apiClient } from '../../services/api';
import type { DepartmentResponse, ProvinceResponse, DistrictResponse } from './types';

export const geographicService = {
    getDepartments(): Promise<DepartmentResponse[]> {
        return apiClient.get<DepartmentResponse[]>('/geography/departments');
    },

    // Agregamos el parámetro opcional departmentId
    getProvinces(departmentId?: number): Promise<ProvinceResponse[]> {
        const config = departmentId ? { params: { department_id: departmentId } } : undefined;
        return apiClient.get<ProvinceResponse[]>('/geography/provinces', config);
    },

    // Agregamos el parámetro opcional provinceId
    getDistricts(provinceId?: number): Promise<DistrictResponse[]> {
        const config = provinceId ? { params: { province_id: provinceId } } : undefined;
        return apiClient.get<DistrictResponse[]>('/geography/districts', config);
    },
};