from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app import models, schemas, auth
from app.database import get_db

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/register", response_model=schemas.TokenResponse)
def register(payload: schemas.RegisterRequest, db: Session = Depends(get_db)):
    if db.query(models.User).filter(models.User.email == payload.email).first():
        raise HTTPException(status_code=400, detail="Bu e-posta zaten kayitli")
    if db.query(models.User).filter(models.User.username == payload.username).first():
        raise HTTPException(status_code=400, detail="Bu kullanici adi zaten alinmis")

    user = models.User(
        email=payload.email,
        username=payload.username,
        hashed_password=auth.hash_password(payload.password),
        is_admin=False,
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    token = auth.create_access_token({"sub": user.id})
    return schemas.TokenResponse(access_token=token, is_admin=user.is_admin)


@router.post("/login", response_model=schemas.TokenResponse)
def login(payload: schemas.LoginRequest, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == payload.email).first()
    if not user or not auth.verify_password(payload.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="E-posta veya sifre hatali")
    if user.status == models.UserStatus.blocked:
        raise HTTPException(status_code=403, detail="Hesabiniz engellenmis durumda")

    token = auth.create_access_token({"sub": user.id})
    # Admin yetkisi burada backend tarafinda dogrulanip token'dan bagimsiz
    # olarak response'a eklenir; Android sadece bu alana gore Admin Dashboard'u acar,
    # ama her admin endpoint cagrisinda backend yine token'i tekrar dogrular.
    return schemas.TokenResponse(access_token=token, is_admin=user.is_admin)


@router.get("/me", response_model=schemas.UserOut)
def me(current_user: models.User = Depends(auth.get_current_user)):
    return current_user
