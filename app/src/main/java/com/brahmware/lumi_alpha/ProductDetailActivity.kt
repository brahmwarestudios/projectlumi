package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var product: Product
    private var selectedSize: String = ""
    private var pickupDate: LocalDate? = null
    private var returnDate: LocalDate? = null
    private val displayFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        product = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("product", Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("product")
        } ?: return

        selectedSize = product.availableSizes.firstOrNull() ?: ""

        bindViews()
        setupSizePicker()
        setupDatePicker()
        setupButtons()
    }

    private fun bindViews() {
        findViewById<ImageView>(R.id.productDetailImage).setImageResource(product.imageRes)
        findViewById<TextView>(R.id.productDetailName).text = product.name
        findViewById<TextView>(R.id.productDetailBrand).text = product.brand.uppercase()
        findViewById<TextView>(R.id.productDetailPrice).text = product.formattedPrice
        findViewById<TextView>(R.id.productDetailDescription).text = product.description

        val badge = findViewById<TextView>(R.id.availabilityBadge)
        badge.text = if (product.isAvailable) "Available" else "Unavailable"
        badge.setBackgroundResource(
            if (product.isAvailable) R.drawable.badge_available else R.drawable.badge_unavailable
        )
    }

    private fun setupSizePicker() {
        val spinner = findViewById<Spinner>(R.id.sizePicker)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, product.availableSizes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                selectedSize = product.availableSizes[pos]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDatePicker() {
        val pickupBtn = findViewById<MaterialButton>(R.id.pickupDateButton)
        val returnBtn = findViewById<MaterialButton>(R.id.returnDateButton)
        val rentalSummary = findViewById<TextView>(R.id.rentalSummaryText)

        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Rental Dates")
            .setCalendarConstraints(constraints)
            .build()

        val openPicker = {
            if (!dateRangePicker.isAdded) {
                dateRangePicker.show(supportFragmentManager, "date_range")
            }
        }

        pickupBtn.setOnClickListener { openPicker() }
        returnBtn.setOnClickListener { openPicker() }

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val zone = ZoneId.systemDefault()
            pickupDate = Instant.ofEpochMilli(selection.first).atZone(zone).toLocalDate()
            returnDate = Instant.ofEpochMilli(selection.second).atZone(zone).toLocalDate()

            pickupBtn.text = pickupDate!!.format(displayFmt)
            returnBtn.text = returnDate!!.format(displayFmt)

            val days = java.time.temporal.ChronoUnit.DAYS.between(pickupDate, returnDate).toInt().coerceAtLeast(1)
            val total = product.rentalPricePerDay * days
            rentalSummary.text = "$days day${if (days != 1) "s" else ""}  ·  ₱${"%,d".format(total)} total"
        }
    }

    private fun setupButtons() {
        findViewById<FloatingActionButton>(R.id.backButton).setOnClickListener { finish() }

        val wishlistButton = findViewById<ImageButton>(R.id.wishlistButton)
        refreshWishlistIcon(wishlistButton)
        wishlistButton.setOnClickListener {
            val isNow = WishlistManager.toggle(product)
            refreshWishlistIcon(wishlistButton)
            Toast.makeText(
                this,
                if (isNow) "Added to Wishlist" else "Removed from Wishlist",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<ImageButton>(R.id.cartIconButton).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.rentNowButton).setOnClickListener {
            if (!validateAndBook()) return@setOnClickListener
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.addToCartButton).setOnClickListener {
            if (validateAndBook()) {
                Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.previewButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
        }
    }

    private fun validateAndBook(): Boolean {
        if (pickupDate == null || returnDate == null) {
            Toast.makeText(this, "Please select rental dates", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!product.isAvailable) {
            Toast.makeText(this, "This item is currently unavailable", Toast.LENGTH_SHORT).show()
            return false
        }
        CartManager.addBooking(
            RentalBooking(
                product = product,
                size = selectedSize,
                pickupDate = pickupDate!!,
                returnDate = returnDate!!
            )
        )
        return true
    }

    private fun refreshWishlistIcon(button: ImageButton) {
        button.setImageResource(
            if (WishlistManager.isWishlisted(product.id)) R.drawable.ic_heart_filled
            else R.drawable.ic_heart_outline
        )
    }
}