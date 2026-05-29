export interface Waypoint {
  id: number;
  route_id: number;
  address: string;
  latitud?: number;
  longitud?: number;
  order_sequence: number;
  client_id: number;
  status: 'PENDIENTE' | 'VISITA' | 'CANCELADA';
  visited_at?: string;
  comment?: string;
}

export interface WaypointCreate {
  address: string;
  latitud?: number;
  longitud?: number;
  order_sequence: number;
  client_id: number;
  status?: string;
  comment?: string;
}

export interface Route {
  id: number;
  name: string;
  scheduled_date: string;
  user_id: number;
  active: boolean;
  waypoints: Waypoint[];
  created_at: string;
  updated_at: string;
}

export interface RouteCreate {
  name: string;
  scheduled_date: string;
  user_id: number;
  active?: boolean;
  waypoints: WaypointCreate[];
}
