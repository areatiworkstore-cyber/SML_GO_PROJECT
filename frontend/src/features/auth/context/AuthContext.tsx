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
    role: string;
    fullName: string;
}

interface AuthContextType {
    isAuthenticated: boolean;
    user: UserSession | null;
    token: string | null;
    login: (token: string, role: string) => Promise<void>;
    logout: () => void;
    loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
    const [user, setUser] = useState<UserSession | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    // Función para obtener los datos actualizados del endpoint /users/me
    const fetchUserData = async (role: string) => {
        try {
            const data = await apiClient.get<{
                id: number;
                code: string;
                first_name: string;
                second_name?: string;
                first_surname: string;
                second_surname?: string;
                email?: string;
            }>('/users/me');

            // Construcción dinámica del nombre sin fallbacks estáticos en duro
            const calculatedName = `${data.first_name} ${data.first_surname}`.trim();

            const fullUserSession: UserSession = {
                ...data,
                role,
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
        const handleUnauthorized = () => {
            logout();
        };
        window.addEventListener('auth:unauthorized', handleUnauthorized);

        const savedUser = localStorage.getItem('user');
        const savedRoles = localStorage.getItem('user_roles');

        if (token && savedUser && savedRoles) {
            setUser(JSON.parse(savedUser));
            setLoading(false);
        } else if (token && savedRoles) {
            const roles = JSON.parse(savedRoles);
            fetchUserData(roles[0] || 'USER');
        } else {
            setLoading(false);
        }

        return () => {
            window.removeEventListener('auth:unauthorized', handleUnauthorized);
        };
    }, [token]);

    const login = async (newToken: string, role: string) => {
        localStorage.setItem('token', newToken);
        localStorage.setItem('user_roles', JSON.stringify([role]));
        setToken(newToken);
        setLoading(true);
        await fetchUserData(role);
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('user_roles');
        setToken(null);
        setUser(null);
        setLoading(false);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated: !!token, user, token, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth debe usarse dentro de un AuthProvider');
    return context;
};