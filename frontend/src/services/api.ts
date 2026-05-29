const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000/api/v1';

interface FetchOptions extends RequestInit {
  params?: Record<string, string | number | boolean>;
}

// 1. Extraemos la función request como una función pura y tipada de forma aislada.
// Esto evita que TypeScript se confunda con la palabra clave 'this' dentro del objeto.
async function executeRequest<T>(endpoint: string, options: FetchOptions = {}): Promise<T> {
  const { params, headers, ...restOptions } = options;

  // Build URL with query params if any
  let url = `${API_BASE_URL}${endpoint}`;
  if (params) {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
      searchParams.append(key, String(val));
    });
    url += `?${searchParams.toString()}`;
  }

  // Default headers, including authorization token
  const token = localStorage.getItem('token');
  const defaultHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const config: RequestInit = {
    headers: { ...defaultHeaders, ...headers },
    ...restOptions,
  };

  const response = await fetch(url, config);

  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('user_roles');

    if (!endpoint.includes('/auth/login')) {
      window.location.reload();
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
    return executeRequest<T>(endpoint, {
      ...options,
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    });
  },

  put<T>(endpoint: string, body?: any, options?: Omit<FetchOptions, 'method' | 'body'>): Promise<T> {
    return executeRequest<T>(endpoint, {
      ...options,
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  },

  delete<T>(endpoint: string, options?: Omit<FetchOptions, 'method' | 'body'>): Promise<T> {
    return executeRequest<T>(endpoint, { ...options, method: 'DELETE' });
  },
};