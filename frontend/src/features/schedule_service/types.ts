export interface ClientScheduleResponse {
    id: number;
    client_id: number;
    user_id: number;
    day: string; // Formato YYYY-MM-DD
    start_time: string; // Formato HH:MM:SS
    observation: string;
    active: boolean;
}

export interface ClientScheduleCreate {
    client_id: number;
    user_id: number;
    day: string;
    start_time: string;
    observation?: string;
    active?: boolean;
}