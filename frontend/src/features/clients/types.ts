export interface Client {
  id: number;
  code: string;
  name: string;
  document_type_id: number;
  document_number: string;
  address: string;
  district_id: number;
  business_type_id: number;
  client_group_id: number;
  cellphone?: string;
  telephone?: string;
  active: boolean;
  user_id: number;
  latitud?: number;
  longitud?: number;
  observation?: string;
  created_at: string;
  updated_at: string;
}

export interface ClientCreate {
  code: string;
  name: string;
  document_type_id: number;
  document_number: string;
  address: string;
  district_id: number;
  business_type_id: number;
  client_group_id: number;
  cellphone?: string;
  telephone?: string;
  active?: boolean;
  latitud?: number;
  longitud?: number;
  user_id: number;
  supplier_id?: number;
  observation?: string;
}

export interface ClientUpdate {
  name?: string;
  address?: string;
  district_id?: number;
  business_type_id?: number;
  client_group_id?: number;
  cellphone?: string;
  telephone?: string;
  active?: boolean;
  latitud?: number;
  longitud?: number;
  user_id?: number;
  supplier_id?: number;
  observation?: string;
}

export interface ClientResponse {
  id: number;
  code: string;
  name: string;
  document_number: string;
  document_type_id: number;
  cellphone: string | null;
  telephone: string | null;
  active: boolean;
  user_id: number;
  supplier_id: number | null;
  address: string;
  district_id: number;
  business_type_id: number;
  client_group_id: number;
  observation: string | null;
  latitud?: number | null;
  longitud?: number | null;
}

export interface BusinessType {
  id: number;
  description: string;
}

export interface ClientGroup {
  id: number;
  description: string;
}