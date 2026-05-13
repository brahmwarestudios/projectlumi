package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If already logged in, skip to the right screen
        if (SessionManager.isLoggedIn(this)) {
            routeByRole()
            return
        }

        setContentView(R.layout.activity_login)

        val emailField    = findViewById<TextInputEditText>(R.id.emailField)
        val passwordField = findViewById<TextInputEditText>(R.id.passwordField)
        val emailLayout   = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout= findViewById<TextInputLayout>(R.id.passwordLayout)
        val errorText     = findViewById<TextView>(R.id.loginError)
        val loginBtn      = findViewById<MaterialButton>(R.id.loginButton)
        val signUpLink    = findViewById<TextView>(R.id.signUpLink)
        val forgotPw      = findViewById<TextView>(R.id.forgotPassword)

        loginBtn.setOnClickListener {
            val email    = emailField.text.toString()
            val password = passwordField.text.toString()

            // Basic validation
            var valid = true
            if (email.isBlank()) { emailLayout.error = "Required"; valid = false }
            else emailLayout.error = null

            if (password.isBlank()) { passwordLayout.error = "Required"; valid = false }
            else passwordLayout.error = null

            if (!valid) return@setOnClickListener

            loginBtn.isEnabled = false
            loginBtn.text = "Logging in…"
            errorText.visibility = View.GONE

            lifecycleScope.launch {
                val result = AuthManager.signIn(email, password)
                loginBtn.isEnabled = true
                loginBtn.text = "Log In"

                if (result.success && result.session != null) {
                    SessionManager.saveSession(this@LoginActivity, result.session)
                    routeByRole()
                } else {
                    errorText.text = result.error ?: "Login failed."
                    errorText.visibility = View.VISIBLE
                }
            }
        }

        signUpLink.setOnClickListener {
            navigateTo(Intent(this, SignUpActivity::class.java))
        }

        forgotPw.setOnClickListener {
            // TODO: implement password reset with Supabase
        }
    }

    private fun routeByRole() {
        val dest = if (SessionManager.isAdmin(this)) {
            Intent(this, AdminDashboardActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }
        navigateTo(dest, finishCurrent = true)
    }
}