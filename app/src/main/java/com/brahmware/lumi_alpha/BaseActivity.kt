package com.brahmware.lumi_alpha

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

/**
 * All activities extend BaseActivity to get:
 * - Consistent slide transitions
 * - Shared navigation helpers
 */
open class BaseActivity : AppCompatActivity() {

    /** Navigate forward with slide-in-right transition */
    fun navigateTo(intent: Intent, finishCurrent: Boolean = false) {
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        if (finishCurrent) finish()
    }

    /** Call this instead of finish() to get the back slide animation */
    fun navigateBack() {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}