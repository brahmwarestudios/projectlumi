package com.brahmware.lumi_alpha

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages the current user session locally.
 * Will be replaced by Supabase auth tokens in the next phase.
 */
object SessionManager {

    private const val PREF_NAME = "lumi_session"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ROLE = "user_role"

    enum class Role { USER, ADMIN }

    data class UserSession(
        val userId: String,
        val name: String,
        val email: String,
        val role: Role
    )

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(context: Context, session: UserSession) {
        prefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, session.userId)
            putString(KEY_USER_NAME, session.name)
            putString(KEY_USER_EMAIL, session.email)
            putString(KEY_USER_ROLE, session.role.name)
            apply()
        }
    }

    fun getSession(context: Context): UserSession? {
        val p = prefs(context)
        if (!p.getBoolean(KEY_IS_LOGGED_IN, false)) return null
        return UserSession(
            userId = p.getString(KEY_USER_ID, "") ?: "",
            name   = p.getString(KEY_USER_NAME, "") ?: "",
            email  = p.getString(KEY_USER_EMAIL, "") ?: "",
            role   = Role.valueOf(p.getString(KEY_USER_ROLE, Role.USER.name) ?: Role.USER.name)
        )
    }

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_LOGGED_IN, false)

    fun isAdmin(context: Context): Boolean =
        getSession(context)?.role == Role.ADMIN

    fun clearSession(context: Context) {
        prefs(context).edit().clear().apply()
    }
}