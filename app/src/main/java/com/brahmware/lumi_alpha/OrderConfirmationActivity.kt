package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class OrderConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        val total = intent.getStringExtra("order_total") ?: ""
        val name = intent.getStringExtra("order_name") ?: ""
        val address = intent.getStringExtra("order_address") ?: ""
        val count = intent.getIntExtra("order_count", 0)

        findViewById<TextView>(R.id.confirmName).text = "Thank you, $name!"
        findViewById<TextView>(R.id.confirmDetails).text =
            "$count item${if (count != 1) "s" else ""} · $total"
        findViewById<TextView>(R.id.confirmAddress).text = "Shipping to: $address"

        // Back to home — clear the back stack
        findViewById<MaterialButton>(R.id.continueShopping).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    // Prevent going back to checkout after order is placed
    override fun onBackPressed() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}