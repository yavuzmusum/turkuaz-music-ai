from datetime import datetime, timedelta

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app import models, schemas, auth
from app.database import get_db

router = APIRouter(prefix="/admin", tags=["admin"])
# Not: bu router'daki her endpoint auth.get_current_admin kullanir; yani
# admin yetkisi UI'da degil, HER istekte backend tarafinda dogrulanir.


@router.get("/dashboard", response_model=schemas.DashboardStats)
def dashboard(db: Session = Depends(get_db), admin: models.User = Depends(auth.get_current_admin)):
    since = datetime.utcnow() - timedelta(days=1)
    total_users = db.query(models.User).count()
    active_users = db.query(models.User).filter(models.User.status == models.UserStatus.active).count()
    total_generations = db.query(models.GenerationLog).filter(models.GenerationLog.success.is_(True)).count()
    generations_today = (
        db.query(models.GenerationLog)
        .filter(models.GenerationLog.success.is_(True), models.GenerationLog.created_at >= since)
        .count()
    )
    maintenance = db.query(models.SystemSetting).filter(models.SystemSetting.key == "maintenance_mode").first()
    system_status = "maintenance" if maintenance and maintenance.value == "true" else "operational"

    return schemas.DashboardStats(
        total_users=total_users,
        active_users=active_users,
        total_generations=total_generations,
        generations_today=generations_today,
        system_status=system_status,
    )


@router.get("/users", response_model=list[schemas.UserAdminOut])
def list_users(db: Session = Depends(get_db), admin: models.User = Depends(auth.get_current_admin)):
    users = db.query(models.User).order_by(models.User.created_at.desc()).all()
    return [
        schemas.UserAdminOut(
            id=u.id, email=u.email, username=u.username, is_admin=u.is_admin,
            status=u.status.value, created_at=u.created_at, track_count=len(u.tracks),
        )
        for u in users
    ]


@router.patch("/users/{user_id}/status", response_model=schemas.UserOut)
def update_user_status(
    user_id: str,
    payload: schemas.UpdateUserStatusRequest,
    db: Session = Depends(get_db),
    admin: models.User = Depends(auth.get_current_admin),
):
    target = db.query(models.User).filter(models.User.id == user_id).first()
    if not target:
        raise HTTPException(status_code=404, detail="Kullanici bulunamadi")
    if payload.status not in ("active", "blocked"):
        raise HTTPException(status_code=400, detail="Gecersiz durum")
    if target.is_admin:
        raise HTTPException(status_code=400, detail="Admin hesabi engellenemez")

    target.status = models.UserStatus(payload.status)
    db.commit()
    db.refresh(target)
    return target


@router.get("/music/reported")
def reported_music(db: Session = Depends(get_db), admin: models.User = Depends(auth.get_current_admin)):
    tracks = db.query(models.Track).filter(models.Track.is_reported.is_(True)).all()
    return [{"id": t.id, "title": t.title, "owner_id": t.owner_id, "created_at": t.created_at} for t in tracks]


@router.delete("/music/{track_id}")
def admin_delete_track(track_id: str, db: Session = Depends(get_db), admin: models.User = Depends(auth.get_current_admin)):
    track = db.query(models.Track).filter(models.Track.id == track_id).first()
    if not track:
        raise HTTPException(status_code=404, detail="Parca bulunamadi")
    db.delete(track)
    db.commit()
    return {"deleted": True}


@router.get("/settings", response_model=list[schemas.SystemSettingIn])
def get_settings(db: Session = Depends(get_db), admin: models.User = Depends(auth.get_current_admin)):
    settings_rows = db.query(models.SystemSetting).all()
    return [schemas.SystemSettingIn(key=s.key, value=s.value or "") for s in settings_rows]


@router.put("/settings", response_model=schemas.SystemSettingIn)
def update_setting(payload: schemas.SystemSettingIn, db: Session = Depends(get_db), admin: models.User = Depends(auth.get_current_admin)):
    """Bakim modu (key=maintenance_mode, value=true/false), gunluk uretim
    limiti (key=daily_generation_limit), duyurular (key=announcement) gibi
    ayarlar bu tek endpoint uzerinden yonetilir."""
    row = db.query(models.SystemSetting).filter(models.SystemSetting.key == payload.key).first()
    if row:
        row.value = payload.value
    else:
        row = models.SystemSetting(key=payload.key, value=payload.value)
        db.add(row)
    db.commit()
    return payload
