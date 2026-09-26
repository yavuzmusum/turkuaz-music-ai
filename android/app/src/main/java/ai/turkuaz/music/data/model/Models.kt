package ai.turkuaz.music.data.model

data class RegisterRequest(val email: String, val username: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class TokenResponse(val access_token: String, val token_type: String, val is_admin: Boolean)

data class GenerateRequest(
    val prompt: String,
    val title: String? = null,
    val duration_seconds: Int = 15
)

data class EditRequest(val instruction: String)
data class RenameRequest(val title: String)

data class TrackVersion(
    val id: String,
    val version_number: Int,
    val prompt_used: String,
    val file_url: String,
    val cover_url: String?,
    val duration_seconds: Int,
    val created_at: String
)

data class Track(
    val id: String,
    val title: String,
    val original_prompt: String,
    val is_favorite: Boolean,
    val created_at: String,
    val latest_version: TrackVersion?,
    val version_count: Int
)

data class TrackDetail(
    val id: String,
    val title: String,
    val original_prompt: String,
    val is_favorite: Boolean,
    val created_at: String,
    val latest_version: TrackVersion?,
    val version_count: Int,
    val versions: List<TrackVersion>
)

// ---- Admin ----

data class DashboardStats(
    val total_users: Int,
    val active_users: Int,
    val total_generations: Int,
    val generations_today: Int,
    val system_status: String
)

data class AdminUser(
    val id: String,
    val email: String,
    val username: String,
    val is_admin: Boolean,
    val status: String,
    val created_at: String,
    val track_count: Int
)

data class UpdateUserStatusRequest(val status: String)
data class SystemSetting(val key: String, val value: String)
