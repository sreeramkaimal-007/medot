package com.example.data.remote.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.DayDialDatabase
import com.example.data.model.DoseLog
import com.example.data.model.Medication
import com.example.data.model.Schedule
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class SupabaseManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun normalizeUrl(input: String): String {
        var url = input.trim()
        if (url.endsWith("/")) url = url.dropLast(1)
        if (url.endsWith("/rest/v1")) url = url.removeSuffix("/rest/v1")
        if (url.endsWith("/")) url = url.dropLast(1)
        return url
    }

    val supabaseUrl: String
        get() {
            val custom = prefs.getString(KEY_CUSTOM_URL, null)?.trim()
            val raw = when {
                !custom.isNullOrBlank() -> custom
                else -> try {
                    BuildConfig.SUPABASE_URL.trim()
                } catch (_: Exception) {
                    ""
                }
            }.ifBlank { "https://uqxvvttfdhqhtmvgcjwm.supabase.co" }

            return normalizeUrl(raw)
        }

    val supabaseAnonKey: String
        get() {
            val custom = prefs.getString(KEY_CUSTOM_ANON_KEY, null)?.trim()
            return when {
                !custom.isNullOrBlank() -> custom
                else -> try {
                    BuildConfig.SUPABASE_ANON_KEY.trim()
                } catch (_: Exception) {
                    ""
                }
            }.ifBlank { "sb_publishable_Dy20ZkXKHcf562_39ojPag_5SrjgbRE" }
        }

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() &&
                supabaseUrl.startsWith("http") &&
                !supabaseUrl.contains("your-project.supabase.co") &&
                supabaseAnonKey.isNotBlank() &&
                !supabaseAnonKey.contains("your-supabase-anon-key-here")

    val isAuthenticated: Boolean
        get() = !accessToken.isNullOrBlank()

    val currentUserId: String?
        get() = prefs.getString(KEY_USER_ID, null)

    val currentUserEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)

    val currentUserName: String?
        get() = prefs.getString(KEY_USER_NAME, null)

    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)

    val lastSyncEpochMillis: Long
        get() = prefs.getLong(KEY_LAST_SYNC_MILLIS, 0L)

    private var cachedApi: SupabaseApi? = null
    private var lastApiUrl: String? = null
    private var lastApiKey: String? = null

    val api: SupabaseApi?
        get() {
            if (!isConfigured) return null
            val currentUrl = supabaseUrl
            val currentKey = supabaseAnonKey

            if (cachedApi != null && lastApiUrl == currentUrl && lastApiKey == currentKey) {
                return cachedApi
            }

            val baseUrl = if (currentUrl.endsWith("/")) currentUrl else "$currentUrl/"

            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val token = accessToken ?: currentKey
                val request = original.newBuilder()
                    .header("apikey", currentKey)
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val newApi = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SupabaseApi::class.java)

            cachedApi = newApi
            lastApiUrl = currentUrl
            lastApiKey = currentKey
            return newApi
        }

    fun saveCredentials(url: String, anonKey: String) {
        val cleanUrl = normalizeUrl(url)
        val cleanKey = anonKey.trim()
        prefs.edit()
            .putString(KEY_CUSTOM_URL, cleanUrl)
            .putString(KEY_CUSTOM_ANON_KEY, cleanKey)
            .apply()
        cachedApi = null
    }

    fun hasCustomCredentials(): Boolean {
        return !prefs.getString(KEY_CUSTOM_ANON_KEY, null).isNullOrBlank()
    }

    suspend fun signUp(email: String, password: String, fullName: String): Result<SupabaseAuthResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase credentials not configured in .env"))
        }
        val service = api ?: return@withContext Result.failure(IllegalStateException("Supabase API unavailable"))

        try {
            val response = service.signUp(
                SupabaseAuthRequest(
                    email = email,
                    password = password,
                    data = mapOf("full_name" to fullName)
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                saveSession(body, email, fullName)
                Result.success(body)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Sign up failed with code ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing up to Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<SupabaseAuthResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase credentials not configured in .env"))
        }
        val service = api ?: return@withContext Result.failure(IllegalStateException("Supabase API unavailable"))

        try {
            val response = service.signInWithPassword(
                SupabaseAuthRequest(
                    email = email,
                    password = password
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val name = body.user?.userMetadata?.get("full_name") ?: email.substringBefore("@")
                saveSession(body, email, name)
                Result.success(body)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Sign in failed with code ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in to Supabase", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .apply()
    }

    suspend fun syncAll(database: DayDialDatabase): Result<SyncResult> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase is not configured"))
        }
        val service = api ?: return@withContext Result.failure(IllegalStateException("API unavailable"))
        val userId = currentUserId

        try {
            // 1. Sync Medications
            val localMeds = database.medicationDao().getAllMedications().first()
            var syncedMedsCount = 0
            if (localMeds.isNotEmpty()) {
                val medDtos = localMeds.map { med ->
                    SupabaseMedicationDto(
                        userId = userId,
                        localId = med.id,
                        name = med.name,
                        dosage = med.dosage,
                        pillColor = null,
                        type = med.type.name,
                        foodInstruction = med.foodInstruction.name,
                        notes = med.notes,
                        isActive = med.isActive
                    )
                }
                val resp = service.insertMedications(medDtos)
                if (resp.isSuccessful) {
                    syncedMedsCount = medDtos.size
                }
            }

            // 2. Sync Schedules
            val localSchedules = database.scheduleDao().getAllActiveSchedules().first()
            var syncedSchedulesCount = 0
            if (localSchedules.isNotEmpty()) {
                val scheduleDtos = localSchedules.map { s ->
                    SupabaseScheduleDto(
                        userId = userId,
                        medicationLocalId = s.medicationId,
                        hour = s.hour,
                        minute = s.minute,
                        daysOfWeekMask = s.daysOfWeekMask,
                        isActive = s.isActive
                    )
                }
                val resp = service.insertSchedules(scheduleDtos)
                if (resp.isSuccessful) {
                    syncedSchedulesCount = scheduleDtos.size
                }
            }

            // 3. Sync Dose Logs
            val localLogs = database.doseLogDao().getAllDoseLogs().first()
            var syncedLogsCount = 0
            if (localLogs.isNotEmpty()) {
                val logDtos = localLogs.take(50).map { log ->
                    SupabaseDoseLogDto(
                        userId = userId,
                        medicationName = log.medicationName,
                        dosage = log.dosage,
                        scheduledEpochMillis = log.scheduledEpochMillis,
                        status = log.status.name,
                        actionEpochMillis = log.actionEpochMillis,
                        notes = log.notes
                    )
                }
                val resp = service.insertDoseLogs(logDtos)
                if (resp.isSuccessful) {
                    syncedLogsCount = logDtos.size
                }
            }

            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_SYNC_MILLIS, now).apply()

            Result.success(SyncResult(syncedMedsCount, syncedSchedulesCount, syncedLogsCount, now))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }

    private fun saveSession(auth: SupabaseAuthResponse, email: String, name: String) {
        val editor = prefs.edit()
        auth.accessToken?.let { editor.putString(KEY_ACCESS_TOKEN, it) }
        auth.refreshToken?.let { editor.putString(KEY_REFRESH_TOKEN, it) }
        auth.user?.id?.let { editor.putString(KEY_USER_ID, it) }
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_NAME, name)
        editor.apply()
    }

    companion object {
        private const val TAG = "SupabaseManager"
        private const val PREFS_NAME = "medot_supabase_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_LAST_SYNC_MILLIS = "last_sync_millis"
        private const val KEY_CUSTOM_URL = "custom_supabase_url"
        private const val KEY_CUSTOM_ANON_KEY = "custom_supabase_anon_key"

        @Volatile
        private var INSTANCE: SupabaseManager? = null

        fun getInstance(context: Context): SupabaseManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SupabaseManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        const val SQL_SCHEMA_SETUP: String = """
-- ========================================================
-- medot Supabase Database Schema
-- Run this in your Supabase project's SQL Editor:
-- ========================================================

-- 1. Medications Table
create table if not exists public.medications (
  id bigint generated by default as identity primary key,
  user_id uuid references auth.users(id) on delete cascade not null,
  local_id bigint not null,
  name text not null,
  dosage text not null,
  pill_color text,
  type text default 'PILL',
  food_instruction text default 'NO_RESTRICTIONS',
  notes text default '',
  is_active boolean default true,
  created_at timestamptz default now()
);

-- 2. Schedules Table
create table if not exists public.schedules (
  id bigint generated by default as identity primary key,
  user_id uuid references auth.users(id) on delete cascade not null,
  medication_local_id bigint not null,
  hour int not null,
  minute int not null,
  days_of_week_mask int default 127,
  is_active boolean default true,
  created_at timestamptz default now()
);

-- 3. Dose Logs Table
create table if not exists public.dose_logs (
  id bigint generated by default as identity primary key,
  user_id uuid references auth.users(id) on delete cascade not null,
  medication_name text not null,
  dosage text not null,
  scheduled_epoch_millis bigint not null,
  status text not null,
  action_epoch_millis bigint,
  notes text default '',
  created_at timestamptz default now()
);

-- Enable Row Level Security (RLS)
alter table public.medications enable row level security;
alter table public.schedules enable row level security;
alter table public.dose_logs enable row level security;

-- Policies for Authenticated Users
create policy "Users can manage their own medications"
  on public.medications for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "Users can manage their own schedules"
  on public.schedules for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "Users can manage their own dose logs"
  on public.dose_logs for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);
"""
    }
}

data class SyncResult(
    val medicationsCount: Int,
    val schedulesCount: Int,
    val doseLogsCount: Int,
    val syncTimestampMillis: Long
)
