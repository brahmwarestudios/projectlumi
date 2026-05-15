package com.brahmware.lumi_alpha

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ProductDetailActivity : BaseActivity() {

    private lateinit var product: Product
    private var selectedSize: String = ""
    private var pickupDate: LocalDate? = null
    private var returnDate: LocalDate? = null
    private val displayFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy")

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
        setupSizeChips()
        setupDateFields()
        setupButtons()
    }

    private fun bindViews() {
        findViewById<ImageView>(R.id.productDetailImage).setImageResource(product.imageRes)
        listOf(R.id.thumb1, R.id.thumb2, R.id.thumb3, R.id.thumb4).forEach { id ->
            findViewById<ImageView>(id).setImageResource(product.imageRes)
        }
        findViewById<TextView>(R.id.productDetailName).text = product.name
        findViewById<TextView>(R.id.productDetailPrice).text = product.formattedPrice
        findViewById<TextView>(R.id.productDetailDescription).text = product.description

        // Wishlist button
        val wishlistBtn = findViewById<ImageButton>(R.id.wishlistButton)
        updateWishlistIcon(wishlistBtn, WishlistManager.isWishlisted(product.id))
        wishlistBtn.setOnClickListener {
            val nowWishlisted = WishlistManager.toggle(product)
            updateWishlistIcon(wishlistBtn, nowWishlisted)
        }
    }

    private fun updateWishlistIcon(btn: ImageButton, wishlisted: Boolean) {
        btn.setImageResource(
            if (wishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
        btn.imageTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(if (wishlisted) "#C084FC" else "#7A6652")
        )
    }

    private fun setupSizeChips() {
        val container = findViewById<LinearLayout>(R.id.sizeContainer)
        container.removeAllViews()

        product.availableSizes.forEach { size ->
            val btn = Button(this).apply {
                text = size
                textSize = 13f
                setTextColor(android.graphics.Color.parseColor("#2C1810"))
                background = getDrawable(R.drawable.size_chip_unselected)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 0, 16, 0)
                layoutParams = lp
                setPadding(32, 16, 32, 16)
            }
            btn.setOnClickListener {
                selectedSize = size
                for (i in 0 until container.childCount) {
                    (container.getChildAt(i) as? Button)?.apply {
                        background = getDrawable(R.drawable.size_chip_unselected)
                        setTextColor(android.graphics.Color.parseColor("#2C1810"))
                    }
                }
                btn.background = getDrawable(R.drawable.size_chip_selected)
                btn.setTextColor(android.graphics.Color.WHITE)
            }
            container.addView(btn)
        }
        (container.getChildAt(0) as? Button)?.apply {
            background = getDrawable(R.drawable.size_chip_selected)
            setTextColor(android.graphics.Color.WHITE)
        }
    }

    private fun setupDateFields() {
        val rentField     = findViewById<TextInputEditText>(R.id.rentDateField)
        val returnField   = findViewById<TextInputEditText>(R.id.returnDateField)
        val extendedCheck = findViewById<CheckBox>(R.id.extendedRentalCheck)

        rentField.setOnClickListener {
            showDatePicker { date ->
                pickupDate = date
                rentField.setText(date.format(displayFmt))
                if (returnDate == null) {
                    val suggested = date.plusDays(1)
                    returnDate = suggested
                    returnField.setText(suggested.format(displayFmt))
                }
            }
        }

        returnField.setOnClickListener {
            showDatePicker { date ->
                val maxDays = if (extendedCheck.isChecked) 30L else 7L
                if (pickupDate != null && date.isAfter(pickupDate!!.plusDays(maxDays))) {
                    Toast.makeText(this, "Max $maxDays days rental period", Toast.LENGTH_SHORT).show()
                    return@showDatePicker
                }
                returnDate = date
                returnField.setText(date.format(displayFmt))
            }
        }
    }

    private fun showDatePicker(onPicked: (LocalDate) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).also { it.datePicker.minDate = System.currentTimeMillis() }.show()
    }

    private fun setupButtons() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener { navigateBack() }

        findViewById<ImageButton>(R.id.cartIconButton).setOnClickListener {
            navigateTo(Intent(this, CartActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.previewButton).setOnClickListener {
            navigateTo(Intent(this, MainActivity::class.java).apply {
                putExtra("product", product)
            })
        }

        findViewById<MaterialButton>(R.id.addToCartButton).setOnClickListener {
            if (validateAndBook()) {
                Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            }
        }

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { navigateBack(); true }
                R.id.nav_cart    -> { navigateTo(Intent(this, CartActivity::class.java)); false }
                R.id.nav_account -> { navigateTo(Intent(this, AccountActivity::class.java)); false }
                else -> false
            }
        }
    }

    private fun validateAndBook(): Boolean {
        if (pickupDate == null || returnDate == null) {
            Toast.makeText(this, "Please select rental dates", Toast.LENGTH_SHORT).show()
            return false
        }
        CartManager.addBooking(RentalBooking(
            product    = product,
            size       = selectedSize,
            pickupDate = pickupDate!!,
            returnDate = returnDate!!
        ))
        return true
    }
}