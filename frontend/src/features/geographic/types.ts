export interface DepartmentResponse {
    id: number;
    name: string;
}

export interface ProvinceResponse {
    id: number;
    name: string;
    department_id: number;
}

export interface DistrictResponse {
    id: number;
    name: string;
    province_id: number;
}