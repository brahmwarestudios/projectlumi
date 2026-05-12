package com.brahmware.lumi_alpha

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CartAdapter
    private lateinit var totalText: TextView
    private lateinit var emptyView: View
    private lateinit var contentView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView  = findViewById(R.id.cartRecyclerView)
        totalText     = findViewById(R.id.cartTotalText)
        emptyView     = findViewById(R.id.cartEmptyView)
        contentView   = findViewById(R.id.cartContentView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CartAdapter { productId ->
            CartManager.removeBooking(productId)
            refresh()
        }
        recyclerView.adapter = adapter

        findViewById<MaterialButton>(R.id.continueShoppingBtn).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.checkoutButton).setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        setupBottomNav()
        refresh()
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        adapter.updateBookings(CartManager.bookings)
        totalText.text = CartManager.formattedTotal()
        val empty = CartManager.isEmpty()
        emptyView.visibility   = if (empty) View.VISIBLE else View.GONE
        contentView.visibility = if (empty) View.GONE    else View.VISIBLE
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavCart)
        nav.selectedItemId = R.id.nav_cart
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { finish(); true }
                R.id.nav_cart    -> true
                R.id.nav_account -> { startActivity(Intent(this, AccountActivity::class.java)); false }
                else -> false
            }
        }
    }
}

class CartAdapter(
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var bookings: List<RentalBooking> = emptyList()

    fun updateBookings(new: List<RentalBooking>) { bookings = new.toList(); notifyDataSetChanged() }

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView    = view.findViewById(R.id.cartItemImage)
        val name: TextView      = view.findViewById(R.id.cartItemName)
        val size: TextView      = view.findViewById(R.id.cartItemSize)
        val dates: TextView     = view.findViewById(R.id.cartItemDates)
        val price: TextView     = view.findViewById(R.id.cartItemPrice)
        val remove: ImageButton = view.findViewById(R.id.cartRemoveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CartViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false))

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val b = bookings[position]
        holder.image.setImageResource(b.product.imageRes)
        holder.name.text  = b.product.name
        holder.size.text  = "Size: ${b.size}"
        holder.dates.text = "${b.formattedPickup}  →  ${b.formattedReturn}  (${b.rentalDays} day${if (b.rentalDays != 1) "s" else ""})"
        holder.price.text = b.formattedSubtotal
        holder.remove.setOnClickListener { onRemove(b.product.id) }
    }

    override fun getItemCount() = bookings.size
}