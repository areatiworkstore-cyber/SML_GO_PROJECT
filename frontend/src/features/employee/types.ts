import type { Client } from '../clients';

export interface Employee {
    id: number;
    code: string;
    first_name: string;
    last_name: string;
    document_number: string;
    role: 'VENDEDOR' | 'ADMIN';
    email: string;
    phone?: string;
    status: 'ACTIVO' | 'INACTIVO';
    roles?: any[];
}

export interface SellerPortfolio {
    sellerId: number;
    sellerName: string;
    sellerCode: string;
    role: string;
    clientCount: number;
    clients: Client[];
}