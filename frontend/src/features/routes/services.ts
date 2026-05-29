import { apiClient } from '../../services/api';
import type { Route, RouteCreate, Waypoint } from './types';

export const routeService = {
  getRoutes(userId?: number): Promise<Route[]> {
    const params = userId ? { user_id: userId } : undefined;
    return apiClient.get<Route[]>('/routes', { params });
  },

  getRoute(id: number): Promise<Route> {
    return apiClient.get<Route>(`/routes/${id}`);
  },

  createRoute(route: RouteCreate): Promise<Route> {
    return apiClient.post<Route>('/routes', route);
  },

  updateWaypointStatus(
    waypointId: number,
    status: 'VISITA' | 'CANCELADA',
    comment?: string
  ): Promise<Waypoint> {
    return apiClient.put<Waypoint>(`/routes/waypoints/${waypointId}`, {
      status,
      comment,
    });
  },
};
