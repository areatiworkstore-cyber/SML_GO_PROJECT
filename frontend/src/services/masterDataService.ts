import type { DocumentTypeResponse } from "../types";
import { apiClient } from "./api";

export const masterDataService = {
    getDocumentTypes(): Promise<DocumentTypeResponse[]> {
        return apiClient.get<DocumentTypeResponse[]>('/master_data/document-types');
    },
    getRoles(): Promise<any[]> {
        return apiClient.get<any[]>('/master_data/roles');
    },
};