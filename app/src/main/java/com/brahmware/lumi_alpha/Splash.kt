package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep your existing splash layout if you have one,
        // or leave it blank — it shows the cream background from the theme

        Handler(Looper.getMainLooper()).postDelayed({
            val dest = when {
                !SessionManager.isLoggedIn(this) -> LoginActivity::class.java
                SessionManager.isAdmin(this)     -> AdminDashboardActivity::class.java
                else                             -> HomeActivity::class.java
            }
            startActivity(Intent(this, dest))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }, 1500)
    }
}