import { apiClient } from '../../../services/api';
import type { SupplierResponse, SupplierCreate, SupplierUpdate } from '../types/types';


export const supplierService = {
  /**
   * Obtiene la lista completa de proveedores registrados para la caché de búsqueda local
   */
  getSuppliers: async (): Promise<SupplierResponse[]> => {
    const response = await apiClient.get<SupplierResponse[]>('/suppliers');
    return response || [];
  },

  /**
   * Busca proveedores directamente en el backend mediante coincidencia por código (Opcional/Adicional)
   */
  buscarPorCodigo: async (code: string): Promise<SupplierResponse[]> => {
    const response = await apiClient.get<SupplierResponse[]>('/suppliers/search', {
      params: { code }
    });
    return response || [];
  },

  createSupplier: async (supplier: SupplierCreate): Promise<SupplierResponse> => {
    const response = await apiClient.post<SupplierResponse>('/suppliers', supplier);
    return response;
  },

  updateSupplier: async (id: number, supplier: SupplierUpdate): Promise<SupplierResponse> => {
    const response = await apiClient.put<SupplierResponse>(`/suppliers/${id}`, supplier);
    return response;
  },
};