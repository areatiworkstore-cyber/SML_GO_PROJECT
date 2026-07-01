import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import { apiClient } from '../../../services/api';

// ── Tipos ──────────────────────────────────────────────────────────────────────

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

// ── Clave de caché en sessionStorage ──────────────────────────────────────────
// sessionStorage se borra al cerrar la pestaña → comportamiento correcto para sesiones.
// NO almacena el token (está en la cookie HttpOnly del backend).
const SESSION_CACHE_KEY = 'sml_session';

function readSessionCache(): UserSession | null {
    try {
        const raw = sessionStorage.getItem(SESSION_CACHE_KEY);
        return raw ? (JSON.parse(raw) as UserSession) : null;
    } catch {
        return null;
    }
}

function writeSessionCache(session: UserSession) {
    sessionStorage.setItem(SESSION_CACHE_KEY, JSON.stringify(session));
}

function clearSessionCache() {
    sessionStorage.removeItem(SESSION_CACHE_KEY);
    // Limpiar también cualquier residuo de la implementación anterior
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    localStorage.removeItem('user_roles');
}

// ── Tipo interno del backend /users/me ─────────────────────────────────────────
interface MeResponse {
    id: number;
    code: string;
    first_name: string;
    second_name?: string;
    first_surname: string;
    second_surname?: string;
    email?: string;
    roles: string[];
}

// ── Context ────────────────────────────────────────────────────────────────────

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    // Si hay caché, arrancamos con los datos ya disponibles (sin flicker)
    const [user, setUser] = useState<UserSession | null>(readSessionCache);
    const [isAuthenticated, setIsAuthenticated] = useState(() => readSessionCache() !== null);
    // loading=true solo si hay caché que necesita validarse contra el backend
    const [loading, setLoading] = useState<boolean>(() => readSessionCache() !== null);
    const initRan = useRef(false);

    // ── Construye UserSession a partir de la respuesta del backend ─────────────
    function buildSession(data: MeResponse): UserSession {
        return {
            ...data,
            fullName: `${data.first_name} ${data.first_surname}`.trim(),
        };
    }

    // ── Obtiene el perfil del usuario desde /users/me y actualiza estado ───────
    // silent=true durante la verificación inicial para suprimir el evento
    // auth:unauthorized y evitar bucles y errores en consola.
    async function fetchUserData(silent = false) {
        const headers: Record<string, string> = silent
            ? { 'x-silent-auth-check': '1' }
            : {};

        const data = await apiClient.get<MeResponse>('/users/me', { headers });
        const session = buildSession(data);
        writeSessionCache(session);
        setUser(session);
        setIsAuthenticated(true);
    }

    // ── Verificación inicial al montar la app ──────────────────────────────────
    // Patrón SPA correcto:
    // - Sin caché → Login inmediato, 0 peticiones al backend.
    // - Con caché → validación silenciosa para comprobar que la cookie sigue activa.
    useEffect(() => {
        if (initRan.current) return;
        initRan.current = true;

        if (!readSessionCache()) {
            // No hay indicios de sesión: renderizar Login sin spinner, sin red.
            setLoading(false);
            return;
        }

        fetchUserData(true)
            .catch(() => {
                // Cookie expirada o inválida → limpiar sin ruido
                clearSessionCache();
                setUser(null);
                setIsAuthenticated(false);
            })
            .finally(() => setLoading(false));

    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // ── login: llamado por el componente Login tras POST /auth/login exitoso ───
    const login = async () => {
        setLoading(true);
        try {
            await fetchUserData(false);
        } finally {
            setLoading(false);
        }
    };

    // ── logout ─────────────────────────────────────────────────────────────────
    const logout = async () => {
        try {
            await apiClient.post('/auth/logout');
        } catch { /* ignorar errores de red en logout */ }

        clearSessionCache();
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