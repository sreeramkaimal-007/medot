package com.example.data.remote.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseAuthRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    val user: SupabaseUser? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String,
    val email: String? = null,
    @Json(name = "user_metadata") val userMetadata: Map<String, String>? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseMedicationDto(
    val id: Long? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "local_id") val localId: Long,
    val name: String,
    val dosage: String,
    @Json(name = "pill_color") val pillColor: String? = null,
    val type: String = "PILL",
    @Json(name = "food_instruction") val foodInstruction: String = "NO_RESTRICTIONS",
    val notes: String = "",
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SupabaseScheduleDto(
    val id: Long? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "medication_local_id") val medicationLocalId: Long,
    val hour: Int,
    val minute: Int,
    @Json(name = "days_of_week_mask") val daysOfWeekMask: Int = 127,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SupabaseDoseLogDto(
    val id: Long? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "medication_name") val medicationName: String,
    val dosage: String,
    @Json(name = "scheduled_epoch_millis") val scheduledEpochMillis: Long,
    val status: String,
    @Json(name = "action_epoch_millis") val actionEpochMillis: Long? = null,
    val notes: String = ""
)
