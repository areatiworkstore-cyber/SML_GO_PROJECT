let baseEnvUrl = import.meta.env.VITE_API_URL || '';
// Si no empieza con http:// o https://, usamos el origen dinamico actual del navegador + /api/v1
if (!baseEnvUrl.startsWith('http://') && !baseEnvUrl.startsWith('https://')) {
  baseEnvUrl = typeof window !== 'undefined' ? `${window.location.origin}/api/v1` : '/api/v1';
}
const API_BASE_URL = baseEnvUrl;

interface FetchOptions extends RequestInit {
  params?: Record<string, string | number | boolean>;
}

async function executeRequest<T>(endpoint: string, options: FetchOptions = {}): Promise<T> {
  const { params, headers, ...restOptions } = options;

  // Build URL with query params if any
  // Evitamos redirecciones 307 del backend (que el proxy de cPanel deforma a http://127.0.0.1...)
  // agregando una barra diagonal '/' al final de los endpoints base de colección.
  let cleanEndpoint = endpoint;
  if (['/clients', '/routes', '/users', '/client_schedule'].includes(cleanEndpoint)) {
    cleanEndpoint += '/';
  }

  let url = `${API_BASE_URL}${cleanEndpoint}`;
  if (params) {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
      searchParams.append(key, String(val));
    });
    url += `?${searchParams.toString()}`;
  }

  // Detectar si es una verificación silenciosa de sesión inicial.
  // Este header es SOLO interno — no se envía al backend.
  const isSilentAuthCheck = !!(headers as Record<string, string>)?.['x-silent-auth-check'];

  // Default headers — el header interno se excluye del request real
  const isFormData = restOptions.body instanceof FormData;
  const { 'x-silent-auth-check': _silent, ...cleanHeaders } = (headers ?? {}) as Record<string, string>;
  const defaultHeaders: Record<string, string> = {
    ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
    ...cleanHeaders,
  };

  const config: RequestInit = {
    headers: defaultHeaders,
    credentials: 'include',
    ...restOptions,
  };

  const response = await fetch(url, config);

  if (response.status === 401) {
    // Solo limpiar storage y disparar evento global cuando NO es una verificación silenciosa.
    // En la verificación silenciosa (al iniciar la app), el caller maneja el error sin ruido.
    if (!isSilentAuthCheck) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('user_roles');
      window.dispatchEvent(new Event('auth:unauthorized'));
    }
    throw new Error('Sesión expirada o no autorizada.');
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.detail || 'Ha ocurrido un error en la solicitud.');
  }

  // Return JSON or empty object if no content
  if (response.status === 204) {
    return {} as T;
  }

  return response.json() as Promise<T>;
}


// 2. Exportamos el objeto manteniendo exactamente tu interfaz pública original.
// Al llamar a 'executeRequest<T>' de forma directa, el compilador jamás volverá a dar el error "Untyped function calls...".
export const apiClient = {
  request<T>(endpoint: string, options?: FetchOptions): Promise<T> {
    return executeRequest<T>(endpoint, options);
  },

  get<T>(endpoint: string, options?: Omit<FetchOptions, 'method' | 'body'>): Promise<T> {
    return executeRequest<T>(endpoint, { ...options, method: 'GET' });
  },

  post<T>(endpoint: string, body?: any, options?: Omit<FetchOptions, 'method' | 'body'>): Promise<T> {
    return executeRequest<T>(endpoint, { ...options, method: 'POST', body: body ? JSON.stringify(body) : undefined });
  },

  put<T>(endpoint: string, body?: any, options?: Omit<FetchOptions, 'method' | 'body'>): Promise<T> {
    return executeRequest<T>(endpoint, { ...options, method: 'PUT', body: body ? JSON.stringify(body) : undefined });
  },

  delete<T>(endpoint: string, options?: Omit<FetchOptions, 'method' | 'body'>): Promise<T> {
    return executeRequest<T>(endpoint, { ...options, method: 'DELETE' });
  },

  postForm<T>(endpoint: string, formData: FormData, options?: Omit<FetchOptions, 'method' | 'body' | 'headers'>): Promise<T> {
    return executeRequest<T>(endpoint, { ...options, method: 'POST', body: formData });
  },

  patch<T>(endpoint: string, body?: any, options?: Omit<FetchOptions, 'method' | 'body'>): Promise<T> {
    return executeRequest<T>(endpoint, { ...options, method: 'PATCH', body: body ? JSON.stringify(body) : undefined });
  }
};