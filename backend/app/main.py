from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.database import Base, engine, SessionLocal
from app import models, auth
from app.config import settings
from app.routers import auth as auth_router, music as music_router, admin as admin_router

Base.metadata.create_all(bind=engine)

app = FastAPI(title="TURKUAZ AI CORE", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # V1: Android app dogrudan bu API'ye baglanir
    allow_methods=["*"],
    allow_headers=["*"],
)

Path(settings.media_dir).mkdir(parents=True, exist_ok=True)
app.mount("/media", StaticFiles(directory=settings.media_dir), name="media")

app.include_router(auth_router.router)
app.include_router(music_router.router)
app.include_router(admin_router.router)


@app.on_event("startup")
def bootstrap_admin():
    """Ilk calistirmada, .env'de tanimli admin hesabi yoksa otomatik olusturur."""
    db = SessionLocal()
    try:
        existing = db.query(models.User).filter(models.User.email == settings.bootstrap_admin_email).first()
        if not existing:
            admin_user = models.User(
                email=settings.bootstrap_admin_email,
                username="admin",
                hashed_password=auth.hash_password(settings.bootstrap_admin_password),
                is_admin=True,
            )
            db.add(admin_user)
            db.commit()
            print(f"[TURKUAZ AI CORE] Admin hesabi olusturuldu: {settings.bootstrap_admin_email}")
    finally:
        db.close()


@app.get("/")
def root():
    return {"service": "TURKUAZ AI CORE", "status": "operational"}
