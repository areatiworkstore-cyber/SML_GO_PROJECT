import type { Client } from '../clients';

export interface Employee {
    id: number;
    code: string;
    first_name: string;
    document_number: string;
    role: 'VENDEDOR' | 'ADMIN' | 'SUPERVISOR';
    email: string;
    phone?: string;
    status: 'ACTIVO' | 'INACTIVO';
}

export interface SellerPortfolio {
    sellerId: number;
    sellerName: string;
    sellerCode: string;
    role: string;
    clientCount: number;
    clients: Client[];
}