package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CheckoutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Rental summary
        val summaryText = CartManager.bookings.joinToString("\n") { booking ->
            "• ${booking.product.name}  (${booking.size})\n" +
                    "  ${booking.formattedPickup} → ${booking.formattedReturn}" +
                    "  ·  ${booking.formattedSubtotal}"
        }
        findViewById<TextView>(R.id.checkoutItemsSummary).text = summaryText

        val count = CartManager.totalCount
        findViewById<TextView>(R.id.checkoutItemCount).text =
            "$count item${if (count != 1) "s" else ""}"
        findViewById<TextView>(R.id.checkoutTotal).text = CartManager.formattedTotal()

        // Back button is now ImageButton
        findViewById<ImageButton>(R.id.checkoutBackButton).setOnClickListener { navigateBack() }

        val nameField    = findViewById<TextInputEditText>(R.id.fieldFullName)
        val addressField = findViewById<TextInputEditText>(R.id.fieldAddress)
        val cityField    = findViewById<TextInputEditText>(R.id.fieldCity)
        val phoneField   = findViewById<TextInputEditText>(R.id.fieldPhone)

        val nameLayout    = findViewById<TextInputLayout>(R.id.layoutFullName)
        val addressLayout = findViewById<TextInputLayout>(R.id.layoutAddress)
        val cityLayout    = findViewById<TextInputLayout>(R.id.layoutCity)
        val phoneLayout   = findViewById<TextInputLayout>(R.id.layoutPhone)

        findViewById<MaterialButton>(R.id.placeOrderButton).setOnClickListener {
            var valid = true
            if (nameField.text.isNullOrBlank())    { nameLayout.error    = "Required"; valid = false } else nameLayout.error    = null
            if (addressField.text.isNullOrBlank())  { addressLayout.error = "Required"; valid = false } else addressLayout.error = null
            if (cityField.text.isNullOrBlank())     { cityLayout.error    = "Required"; valid = false } else cityLayout.error    = null
            if (phoneField.text.isNullOrBlank())    { phoneLayout.error   = "Required"; valid = false } else phoneLayout.error   = null

            if (valid) {
                val total     = CartManager.formattedTotal()
                val itemCount = CartManager.totalCount
                CartManager.clear()

                val intent = Intent(this, OrderConfirmationActivity::class.java)
                intent.putExtra("order_total", total)
                intent.putExtra("order_name", nameField.text.toString())
                intent.putExtra("order_address", "${addressField.text}, ${cityField.text}")
                intent.putExtra("order_count", itemCount)
                navigateTo(intent, finishCurrent = true)
            }
        }
    }
}