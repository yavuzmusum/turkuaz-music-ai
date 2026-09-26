package ai.turkuaz.music.data.api

import ai.turkuaz.music.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---- Auth ----
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<TokenResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    // ---- Music ----
    @POST("music")
    suspend fun createTrack(@Body body: GenerateRequest): Response<Track>

    @POST("music/{trackId}/edit")
    suspend fun editTrack(@Path("trackId") trackId: String, @Body body: EditRequest): Response<Track>

    @GET("music")
    suspend fun listTracks(@Query("filter") filter: String = "all"): Response<List<Track>>

    @GET("music/{trackId}")
    suspend fun getTrack(@Path("trackId") trackId: String): Response<TrackDetail>

    @PATCH("music/{trackId}/rename")
    suspend fun renameTrack(@Path("trackId") trackId: String, @Body body: RenameRequest): Response<Track>

    @PATCH("music/{trackId}/favorite")
    suspend fun toggleFavorite(@Path("trackId") trackId: String): Response<Track>

    @DELETE("music/{trackId}")
    suspend fun deleteTrack(@Path("trackId") trackId: String): Response<Unit>

    // ---- Admin ----
    @GET("admin/dashboard")
    suspend fun adminDashboard(): Response<DashboardStats>

    @GET("admin/users")
    suspend fun adminListUsers(): Response<List<AdminUser>>

    @PATCH("admin/users/{userId}/status")
    suspend fun adminUpdateUserStatus(@Path("userId") userId: String, @Body body: UpdateUserStatusRequest): Response<Unit>

    @DELETE("admin/music/{trackId}")
    suspend fun adminDeleteTrack(@Path("trackId") trackId: String): Response<Unit>

    @GET("admin/settings")
    suspend fun adminGetSettings(): Response<List<SystemSetting>>

    @PUT("admin/settings")
    suspend fun adminUpdateSetting(@Body body: SystemSetting): Response<SystemSetting>
}
