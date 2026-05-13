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

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val nameField     = findViewById<TextInputEditText>(R.id.signUpNameField)
        val emailField    = findViewById<TextInputEditText>(R.id.signUpEmailField)
        val passwordField = findViewById<TextInputEditText>(R.id.signUpPasswordField)
        val confirmField  = findViewById<TextInputEditText>(R.id.signUpConfirmField)

        val nameLayout    = findViewById<TextInputLayout>(R.id.signUpNameLayout)
        val emailLayout   = findViewById<TextInputLayout>(R.id.signUpEmailLayout)
        val passwordLayout= findViewById<TextInputLayout>(R.id.signUpPasswordLayout)
        val confirmLayout = findViewById<TextInputLayout>(R.id.signUpConfirmLayout)

        val errorText     = findViewById<TextView>(R.id.signUpError)
        val signUpBtn     = findViewById<MaterialButton>(R.id.signUpButton)
        val loginLink     = findViewById<TextView>(R.id.loginLink)

        findViewById<android.widget.ImageButton>(R.id.signUpBackButton).setOnClickListener {
            navigateBack()
        }

        signUpBtn.setOnClickListener {
            val name     = nameField.text.toString().trim()
            val email    = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirm  = confirmField.text.toString()

            var valid = true
            if (name.isBlank())     { nameLayout.error     = "Required"; valid = false } else nameLayout.error = null
            if (email.isBlank())    { emailLayout.error    = "Required"; valid = false } else emailLayout.error = null
            if (password.isBlank()) { passwordLayout.error = "Required"; valid = false } else passwordLayout.error = null
            if (confirm != password){ confirmLayout.error  = "Passwords do not match"; valid = false } else confirmLayout.error = null

            if (!valid) return@setOnClickListener

            signUpBtn.isEnabled = false
            signUpBtn.text = "Creating account…"
            errorText.visibility = View.GONE

            lifecycleScope.launch {
                val result = AuthManager.signUp(name, email, password)
                signUpBtn.isEnabled = true
                signUpBtn.text = "Create Account"

                if (result.success && result.session != null) {
                    SessionManager.saveSession(this@SignUpActivity, result.session)
                    // New users always go to Home
                    navigateTo(Intent(this@SignUpActivity, HomeActivity::class.java), finishCurrent = true)
                } else {
                    errorText.text = result.error ?: "Sign up failed."
                    errorText.visibility = View.VISIBLE
                }
            }
        }

        loginLink.setOnClickListener { navigateBack() }
    }
}