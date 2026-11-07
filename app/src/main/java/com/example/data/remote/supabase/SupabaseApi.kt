package com.example.data.remote.supabase

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    // Auth endpoints
    @Headers("Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: SupabaseAuthRequest
    ): Response<SupabaseAuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    suspend fun signInWithPassword(
        @Body request: SupabaseAuthRequest
    ): Response<SupabaseAuthResponse>

    // PostgREST Database endpoints
    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @POST("rest/v1/medications")
    suspend fun insertMedications(
        @Body medications: List<SupabaseMedicationDto>
    ): Response<List<SupabaseMedicationDto>>

    @GET("rest/v1/medications")
    suspend fun getMedications(
        @Query("select") select: String = "*"
    ): Response<List<SupabaseMedicationDto>>

    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @POST("rest/v1/schedules")
    suspend fun insertSchedules(
        @Body schedules: List<SupabaseScheduleDto>
    ): Response<List<SupabaseScheduleDto>>

    @GET("rest/v1/schedules")
    suspend fun getSchedules(
        @Query("select") select: String = "*"
    ): Response<List<SupabaseScheduleDto>>

    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @POST("rest/v1/dose_logs")
    suspend fun insertDoseLogs(
        @Body doseLogs: List<SupabaseDoseLogDto>
    ): Response<List<SupabaseDoseLogDto>>

    @GET("rest/v1/dose_logs")
    suspend fun getDoseLogs(
        @Query("select") select: String = "*"
    ): Response<List<SupabaseDoseLogDto>>
}
