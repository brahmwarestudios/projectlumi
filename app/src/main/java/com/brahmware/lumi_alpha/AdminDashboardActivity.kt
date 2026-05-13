package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class AdminDashboardActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val session = SessionManager.getSession(this)
        findViewById<TextView>(R.id.adminWelcome).text = "Welcome, ${session?.name}"

        findViewById<MaterialButton>(R.id.adminLogoutButton).setOnClickListener {
            SessionManager.clearSession(this)
            navigateTo(Intent(this, LoginActivity::class.java), finishCurrent = true)
        }
    }
}