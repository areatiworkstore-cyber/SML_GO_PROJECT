from typing import Optional, List
from pydantic import BaseModel, EmailStr

class Token(BaseModel):
    access_token: str
    token_type: str
    roles: List[str]

class TokenData(BaseModel):
    user_id: Optional[str] = None
    roles: List[str] = []

class LoginRequest(BaseModel):
    username: str  # Code or Email
    password: str
