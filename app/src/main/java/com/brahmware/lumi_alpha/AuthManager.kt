package com.brahmware.lumi_alpha

/**
 * Handles authentication logic.
 *
 * Currently uses hardcoded credentials for development.
 * Phase 2: swap signIn() and signUp() bodies with Supabase Auth SDK calls.
 *
 * Supabase swap points are marked with // SUPABASE: comments.
 */
object AuthManager {

    data class AuthResult(
        val success: Boolean,
        val session: SessionManager.UserSession? = null,
        val error: String? = null
    )

    // ── Hardcoded dev accounts ────────────────────────────────────────────────
    // Remove these once Supabase is connected
    private val devAccounts = listOf(
        Triple("admin@lumi.ph",  "admin123",  SessionManager.Role.ADMIN),
        Triple("user@lumi.ph",   "user123",   SessionManager.Role.USER),
        Triple("maria@email.com","password",  SessionManager.Role.USER)
    )
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sign in with email and password.
     * Returns AuthResult with session on success, error message on failure.
     */
    suspend fun signIn(email: String, password: String): AuthResult {
        // SUPABASE: Replace the block below with:
        // val response = supabase.auth.signInWith(Email) {
        //     this.email = email
        //     this.password = password
        // }
        // Then fetch the user's role from your `users` table.

        val trimmedEmail = email.trim().lowercase()
        val match = devAccounts.find {
            it.first == trimmedEmail && it.second == password
        }

        return if (match != null) {
            AuthResult(
                success = true,
                session = SessionManager.UserSession(
                    userId = trimmedEmail,
                    name   = nameFromEmail(trimmedEmail),
                    email  = trimmedEmail,
                    role   = match.third
                )
            )
        } else {
            AuthResult(success = false, error = "Incorrect email or password.")
        }
    }

    /**
     * Sign up a new user account.
     * Role defaults to USER — admin accounts are created manually.
     */
    suspend fun signUp(name: String, email: String, password: String): AuthResult {
        // SUPABASE: Replace with:
        // val response = supabase.auth.signUpWith(Email) {
        //     this.email = email
        //     this.password = password
        // }
        // Then insert a row in your `users` table with role = 'user'.

        if (password.length < 6) {
            return AuthResult(success = false, error = "Password must be at least 6 characters.")
        }
        val alreadyExists = devAccounts.any { it.first == email.trim().lowercase() }
        if (alreadyExists) {
            return AuthResult(success = false, error = "An account with this email already exists.")
        }

        return AuthResult(
            success = true,
            session = SessionManager.UserSession(
                userId = email.trim().lowercase(),
                name   = name.trim(),
                email  = email.trim().lowercase(),
                role   = SessionManager.Role.USER
            )
        )
    }

    private fun nameFromEmail(email: String): String {
        return when (email) {
            "admin@lumi.ph"  -> "Admin"
            "maria@email.com"-> "Maria Santos"
            else -> email.substringBefore("@").replaceFirstChar { it.uppercase() }
        }
    }
}