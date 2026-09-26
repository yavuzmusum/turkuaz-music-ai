from datetime import datetime
from typing import Optional, List

from pydantic import BaseModel, EmailStr, Field


# ---------- Auth ----------

class RegisterRequest(BaseModel):
    email: EmailStr
    username: str = Field(min_length=3, max_length=30)
    password: str = Field(min_length=6)


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    is_admin: bool


class UserOut(BaseModel):
    id: str
    email: str
    username: str
    is_admin: bool
    status: str
    created_at: datetime

    class Config:
        from_attributes = True


# ---------- Music ----------

class GenerateRequest(BaseModel):
    prompt: str = Field(min_length=3, max_length=500)
    title: Optional[str] = None
    duration_seconds: int = Field(default=15, ge=5, le=60)


class EditRequest(BaseModel):
    """Var olan bir track uzerinde AI ile yeniden duzenleme -> yeni versiyon."""
    instruction: str = Field(min_length=3, max_length=300)


class TrackVersionOut(BaseModel):
    id: str
    version_number: int
    prompt_used: str
    file_url: str
    cover_url: Optional[str]
    duration_seconds: int
    created_at: datetime

    class Config:
        from_attributes = True


class TrackOut(BaseModel):
    id: str
    title: str
    original_prompt: str
    is_favorite: bool
    created_at: datetime
    latest_version: Optional[TrackVersionOut]
    version_count: int

    class Config:
        from_attributes = True


class TrackDetailOut(TrackOut):
    versions: List[TrackVersionOut]


class RenameRequest(BaseModel):
    title: str = Field(min_length=1, max_length=100)


# ---------- Admin ----------

class DashboardStats(BaseModel):
    total_users: int
    active_users: int
    total_generations: int
    generations_today: int
    system_status: str


class UserAdminOut(UserOut):
    track_count: int


class UpdateUserStatusRequest(BaseModel):
    status: str  # "active" | "blocked"


class SystemSettingIn(BaseModel):
    key: str
    value: str
