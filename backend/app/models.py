import enum
import uuid
from datetime import datetime

from sqlalchemy import (
    Column, String, Boolean, Integer, DateTime, ForeignKey, Text, Enum
)
from sqlalchemy.orm import relationship

from app.database import Base


def gen_id():
    return str(uuid.uuid4())


class UserStatus(str, enum.Enum):
    active = "active"
    blocked = "blocked"


class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True, default=gen_id)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_admin = Column(Boolean, default=False, nullable=False)
    status = Column(Enum(UserStatus), default=UserStatus.active, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    tracks = relationship("Track", back_populates="owner", cascade="all, delete-orphan")


class Track(Base):
    """Bir 'proje' / sarki. Birden fazla versiyonu olabilir (AI ile tekrar duzenleme)."""
    __tablename__ = "tracks"

    id = Column(String, primary_key=True, default=gen_id)
    owner_id = Column(String, ForeignKey("users.id"), nullable=False)
    title = Column(String, nullable=False, default="Untitled")
    original_prompt = Column(Text, nullable=False)
    is_favorite = Column(Boolean, default=False)
    is_reported = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    owner = relationship("User", back_populates="tracks")
    versions = relationship(
        "TrackVersion", back_populates="track",
        cascade="all, delete-orphan", order_by="TrackVersion.version_number"
    )


class TrackVersion(Base):
    """Her AI uretimi/duzenlemesi yeni bir versiyon olusturur, oncekiler silinmez."""
    __tablename__ = "track_versions"

    id = Column(String, primary_key=True, default=gen_id)
    track_id = Column(String, ForeignKey("tracks.id"), nullable=False)
    version_number = Column(Integer, nullable=False)
    prompt_used = Column(Text, nullable=False)
    file_path = Column(String, nullable=False)
    duration_seconds = Column(Integer, default=0)
    cover_path = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    track = relationship("Track", back_populates="versions")


class GenerationLog(Base):
    """Admin panel istatistikleri ve gunluk limit kontrolu icin."""
    __tablename__ = "generation_logs"

    id = Column(String, primary_key=True, default=gen_id)
    user_id = Column(String, ForeignKey("users.id"), nullable=False)
    track_id = Column(String, ForeignKey("tracks.id"), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    success = Column(Boolean, default=True)


class SystemSetting(Base):
    """Bakim modu, duyurular, generation limiti gibi admin ayarlari."""
    __tablename__ = "system_settings"

    key = Column(String, primary_key=True)
    value = Column(Text, nullable=True)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
