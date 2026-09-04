from typing import Any, List, Union
from pydantic_settings import BaseSettings
from pydantic import AnyHttpUrl, field_validator

class Settings(BaseSettings):
    PROJECT_NAME: str
    VERSION: str
    API_V1_STR: str
    
    # JWT & Security
    SECRET_KEY: str
    ALGORITHM: str
    ACCESS_TOKEN_EXPIRE_MINUTES: int

    # Database
    DATABASE_URL: str

    # SFTP Storage Config
    SFTP_HOST: str
    SFTP_PORT: int
    SFTP_USERNAME: str
    SFTP_PASSPHRASE: str
    SFTP_REMOTE_DIR: str
    MEDIA_PUBLIC_URL_BASE: str

    # CORS
    BACKEND_CORS_ORIGINS: Any = [
        "http://localhost:3000",
        "http://localhost:5173",
        "https://smlgo.sml.com.pe",
        "https://smlgo.workstore.com.pe"
    ]

    @field_validator("BACKEND_CORS_ORIGINS", mode="before")
    @classmethod
    def assemble_cors_origins(cls, v: Any) -> List[str]:
        if isinstance(v, str):
            if not v.strip():
                return [
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "https://smlgo.sml.com.pe",
                    "https://smlgo.workstore.com.pe"
                ]
            if v.startswith("[") and v.endswith("]"):
                import json
                return json.loads(v)
            return [i.strip() for i in v.split(",") if i.strip()]
        elif isinstance(v, list):
            return [str(i) for i in v]
        return []

    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()
