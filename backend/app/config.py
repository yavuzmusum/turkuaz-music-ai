from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    secret_key: str = "insecure-dev-secret-change-me"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 60 * 24 * 7  # 7 gun

    huggingface_api_token: str = ""
    musicgen_model: str = "facebook/musicgen-small"

    database_url: str = "sqlite:///./turkuaz.db"
    media_dir: str = "./media"

    bootstrap_admin_email: str = "admin@turkuaz.ai"
    bootstrap_admin_password: str = "change-this-password"

    # V1 uretim limiti: gunluk kullanici basina kac uretim yapilabilir
    default_daily_generation_limit: int = 10

    class Config:
        env_file = ".env"


settings = Settings()
