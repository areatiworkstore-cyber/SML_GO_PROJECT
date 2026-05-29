from app.models import master_data
from sys import prefix
from fastapi import APIRouter
from app.api.v1.endpoints import auth, users, clients, routes, health, geographic, client_schedule, master_data

api_router = APIRouter()

api_router.include_router(health.router, prefix="/health", tags=["health"])
api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(users.router, prefix="/users", tags=["users"])
api_router.include_router(clients.router, prefix="/clients", tags=["clients"])
api_router.include_router(routes.router, prefix="/routes", tags=["routes"])
api_router.include_router(geographic.router, prefix="/geographic", tags=["geographic"])
api_router.include_router(client_schedule.router, prefix="/client_schedules", tags=["client_schedules"])
api_router.include_router(master_data.router, prefix="/master_data", tags=["master_data"])
