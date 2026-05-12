package com.brahmware.lumi_alpha

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class RulesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rules)
        findViewById<ImageButton>(R.id.rulesBackButton).setOnClickListener { finish() }
    }
}