from datetime import datetime, timedelta
from pathlib import Path

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app import models, schemas, auth, ai_service
from app.database import get_db
from app.config import settings

router = APIRouter(prefix="/music", tags=["music"])


def _to_version_out(v: models.TrackVersion) -> schemas.TrackVersionOut:
    return schemas.TrackVersionOut(
        id=v.id,
        version_number=v.version_number,
        prompt_used=v.prompt_used,
        file_url=f"/media/{Path(v.file_path).name}",
        cover_url=f"/media/{Path(v.cover_path).name}" if v.cover_path else None,
        duration_seconds=v.duration_seconds,
        created_at=v.created_at,
    )


def _to_track_out(t: models.Track) -> schemas.TrackOut:
    latest = t.versions[-1] if t.versions else None
    return schemas.TrackOut(
        id=t.id,
        title=t.title,
        original_prompt=t.original_prompt,
        is_favorite=t.is_favorite,
        created_at=t.created_at,
        latest_version=_to_version_out(latest) if latest else None,
        version_count=len(t.versions),
    )


def _check_daily_limit(db: Session, user: models.User):
    since = datetime.utcnow() - timedelta(days=1)
    count = (
        db.query(models.GenerationLog)
        .filter(models.GenerationLog.user_id == user.id, models.GenerationLog.created_at >= since)
        .count()
    )
    limit = _get_generation_limit(db)
    if count >= limit:
        raise HTTPException(
            status_code=429,
            detail=f"Gunluk uretim limitine ulastiniz ({limit}). Yarin tekrar deneyin.",
        )


def _get_generation_limit(db: Session) -> int:
    setting = db.query(models.SystemSetting).filter(models.SystemSetting.key == "daily_generation_limit").first()
    if setting and setting.value and setting.value.isdigit():
        return int(setting.value)
    return settings.default_daily_generation_limit


def _check_maintenance_mode(db: Session):
    setting = db.query(models.SystemSetting).filter(models.SystemSetting.key == "maintenance_mode").first()
    if setting and setting.value == "true":
        raise HTTPException(status_code=503, detail="Sistem su an bakimda, lutfen daha sonra tekrar deneyin.")


@router.post("", response_model=schemas.TrackOut)
async def create_track(
    payload: schemas.GenerateRequest,
    db: Session = Depends(get_db),
    user: models.User = Depends(auth.get_current_user),
):
    """Ana V1 akisi: tek prompt -> muzik. (bkz. spec 5. Create)"""
    _check_maintenance_mode(db)
    _check_daily_limit(db, user)

    try:
        file_path = await ai_service.generate_music(payload.prompt, payload.duration_seconds)
    except ai_service.MusicGenerationError as e:
        db.add(models.GenerationLog(user_id=user.id, success=False))
        db.commit()
        raise HTTPException(status_code=502, detail=str(e))

    track = models.Track(
        owner_id=user.id,
        title=payload.title or _title_from_prompt(payload.prompt),
        original_prompt=payload.prompt,
    )
    db.add(track)
    db.flush()

    version = models.TrackVersion(
        track_id=track.id,
        version_number=1,
        prompt_used=payload.prompt,
        file_path=file_path,
        duration_seconds=payload.duration_seconds,
    )
    db.add(version)
    db.add(models.GenerationLog(user_id=user.id, track_id=track.id, success=True))
    db.commit()
    db.refresh(track)
    return _to_track_out(track)


@router.post("/{track_id}/edit", response_model=schemas.TrackOut)
async def edit_track(
    track_id: str,
    payload: schemas.EditRequest,
    db: Session = Depends(get_db),
    user: models.User = Depends(auth.get_current_user),
):
    """AI ile tekrar duzenleme -> yeni versiyon, oncekiler silinmez. (bkz. spec 8)"""
    _check_maintenance_mode(db)
    _check_daily_limit(db, user)

    track = _get_owned_track(db, track_id, user)
    combined_prompt = f"{track.original_prompt}. Ek talimat: {payload.instruction}"

    try:
        file_path = await ai_service.generate_music(combined_prompt, duration_seconds=15)
    except ai_service.MusicGenerationError as e:
        db.add(models.GenerationLog(user_id=user.id, success=False))
        db.commit()
        raise HTTPException(status_code=502, detail=str(e))

    next_version_number = len(track.versions) + 1
    version = models.TrackVersion(
        track_id=track.id,
        version_number=next_version_number,
        prompt_used=combined_prompt,
        file_path=file_path,
        duration_seconds=15,
    )
    db.add(version)
    db.add(models.GenerationLog(user_id=user.id, track_id=track.id, success=True))
    db.commit()
    db.refresh(track)
    return _to_track_out(track)


@router.get("", response_model=list[schemas.TrackOut])
def list_tracks(
    filter: str = "all",  # all | recent | favorites
    db: Session = Depends(get_db),
    user: models.User = Depends(auth.get_current_user),
):
    query = db.query(models.Track).filter(models.Track.owner_id == user.id)
    if filter == "favorites":
        query = query.filter(models.Track.is_favorite.is_(True))
    query = query.order_by(models.Track.created_at.desc())
    if filter == "recent":
        query = query.limit(10)
    return [_to_track_out(t) for t in query.all()]


@router.get("/{track_id}", response_model=schemas.TrackDetailOut)
def get_track(track_id: str, db: Session = Depends(get_db), user: models.User = Depends(auth.get_current_user)):
    track = _get_owned_track(db, track_id, user)
    base = _to_track_out(track)
    return schemas.TrackDetailOut(**base.model_dump(), versions=[_to_version_out(v) for v in track.versions])


@router.patch("/{track_id}/rename", response_model=schemas.TrackOut)
def rename_track(track_id: str, payload: schemas.RenameRequest, db: Session = Depends(get_db), user: models.User = Depends(auth.get_current_user)):
    track = _get_owned_track(db, track_id, user)
    track.title = payload.title
    db.commit()
    db.refresh(track)
    return _to_track_out(track)


@router.patch("/{track_id}/favorite", response_model=schemas.TrackOut)
def toggle_favorite(track_id: str, db: Session = Depends(get_db), user: models.User = Depends(auth.get_current_user)):
    track = _get_owned_track(db, track_id, user)
    track.is_favorite = not track.is_favorite
    db.commit()
    db.refresh(track)
    return _to_track_out(track)


@router.delete("/{track_id}")
def delete_track(track_id: str, db: Session = Depends(get_db), user: models.User = Depends(auth.get_current_user)):
    track = _get_owned_track(db, track_id, user)
    db.delete(track)
    db.commit()
    return {"deleted": True}


def _get_owned_track(db: Session, track_id: str, user: models.User) -> models.Track:
    track = db.query(models.Track).filter(models.Track.id == track_id, models.Track.owner_id == user.id).first()
    if not track:
        raise HTTPException(status_code=404, detail="Parca bulunamadi")
    return track


def _title_from_prompt(prompt: str) -> str:
    words = prompt.strip().split()
    return " ".join(words[:5]).capitalize() if words else "Untitled"
