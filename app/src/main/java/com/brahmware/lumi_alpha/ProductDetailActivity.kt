package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductDetailActivity : AppCompatActivity() {

    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val product = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("product", Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("product")
        } ?: return

        // Bind views
        findViewById<ImageView>(R.id.productDetailImage).setImageResource(product.imageRes)
        findViewById<TextView>(R.id.productDetailName).text = product.name
        findViewById<TextView>(R.id.productDetailBrand).text = product.brand.uppercase()
        findViewById<TextView>(R.id.productDetailPrice).text = product.price
        findViewById<TextView>(R.id.productDetailDescription).text = product.description

        // Quantity controls
        val quantityText = findViewById<TextView>(R.id.quantityText)

        findViewById<MaterialButton>(R.id.increaseQty).setOnClickListener {
            quantity++
            quantityText.text = quantity.toString()
        }

        findViewById<MaterialButton>(R.id.decreaseQty).setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityText.text = quantity.toString()
            }
        }

        // Back button
        findViewById<FloatingActionButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Preview button — launches camera with this product preloaded
        findViewById<MaterialButton>(R.id.previewButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
        }
    }
}