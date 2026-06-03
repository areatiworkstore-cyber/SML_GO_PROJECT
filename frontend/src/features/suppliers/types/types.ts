export interface SupplierResponse {
  id: number;
  code: string;
  names: string;
  active: boolean;
  created_at: string;
  updated_at: string;
}

export interface Supplier {
  id: number;
  code: string;
  names: string;
  active: boolean;
  created_at: string;
  updated_at: string;
}

export interface SupplierCreate {
  code: string;
  names: string;
  active: boolean;
}

export interface SupplierUpdate {
  code?: string;
  names?: string;
  active?: boolean;
}
