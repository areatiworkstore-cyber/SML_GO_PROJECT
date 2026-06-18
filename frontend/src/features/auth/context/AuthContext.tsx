import React, { createContext, useContext, useState, useEffect } from 'react';
import { apiClient } from '../../../services/api'; // Ajusta la ruta si es necesario hacia tu api.ts

export interface UserSession {
    id: number;
    code: string;
    first_name: string;
    second_name?: string;
    first_surname: string;
    second_surname?: string;
    email?: string;
    roles: string[];
    fullName: string;
}

interface AuthContextType {
    isAuthenticated: boolean;
    user: UserSession | null;
    login: () => Promise<void>;
    logout: () => Promise<void>;
    loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [user, setUser] = useState<UserSession | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    // Función para obtener los datos actualizados del endpoint /users/me
    const fetchUserData = async () => {
        try {
            const data = await apiClient.get<{
                id: number;
                code: string;
                first_name: string;
                second_name?: string;
                first_surname: string;
                second_surname?: string;
                email?: string;
                roles: string[];
            }>('/users/me');

            // Construcción dinámica del nombre sin fallbacks estáticos en duro
            const calculatedName = `${data.first_name} ${data.first_surname}`.trim();

            const fullUserSession: UserSession = {
                ...data,
                fullName: calculatedName
            };

            localStorage.setItem('user', JSON.stringify(fullUserSession));
            setUser(fullUserSession);
        } catch (error) {
            console.error('Error obteniendo perfil desde /users/me:', error);
            // Si el token expiró o falló la API, forzamos logout preventivo
            logout();
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {

        const initializeAuth = async () => {

            try {
                await fetchUserData();
                setIsAuthenticated(true);
            } catch {

                setIsAuthenticated(false);

                localStorage.removeItem('user');
            }

            setLoading(false);
        };

        initializeAuth();

    }, []);

    const login = async () => {
        setIsAuthenticated(true);
        setLoading(true);
        await fetchUserData();
    };

    const logout = async () => {
        try {
            await apiClient.post('/auth/logout');
        } catch { }

        localStorage.removeItem('user');

        setUser(null);
        setIsAuthenticated(false);
        setLoading(false);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, user, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth debe usarse dentro de un AuthProvider');
    return context;
};