import type { ClientGroup, BusinessType } from "../features/clients";
import type { DocumentTypeResponse } from "../types";
import { apiClient } from "./api";

export const masterDataService = {
    getDocumentTypes(): Promise<DocumentTypeResponse[]> {
        return apiClient.get<DocumentTypeResponse[]>('/master_data/document-types');
    },
    getRoles(): Promise<any[]> {
        return apiClient.get<any[]>('/master_data/roles');
    },
    getBusinessTypes(): Promise<BusinessType[]> {
        return apiClient.get<BusinessType[]>('/master_data/business-types');
    },
    getClientGroups(): Promise<ClientGroup[]> {
        return apiClient.get<ClientGroup[]>('/master_data/client-groups');
  }
};