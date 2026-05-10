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
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CartAdapter
    private lateinit var totalText: TextView
    private lateinit var emptyView: View
    private lateinit var contentView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView = findViewById(R.id.cartRecyclerView)
        totalText = findViewById(R.id.cartTotalText)
        emptyView = findViewById(R.id.cartEmptyView)
        contentView = findViewById(R.id.cartContentView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = CartAdapter(
            onRemove = { productId ->
                CartManager.removeBooking(productId)
                refresh()
            }
        )
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.cartBackButton).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.checkoutButton).setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        adapter.updateBookings(CartManager.bookings)
        totalText.text = CartManager.formattedTotal()
        if (CartManager.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            contentView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            contentView.visibility = View.VISIBLE
        }
    }
}

class CartAdapter(
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var bookings: List<RentalBooking> = emptyList()

    fun updateBookings(newBookings: List<RentalBooking>) {
        bookings = newBookings.toList()
        notifyDataSetChanged()
    }

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.cartItemImage)
        val name: TextView = view.findViewById(R.id.cartItemName)
        val size: TextView = view.findViewById(R.id.cartItemSize)
        val dates: TextView = view.findViewById(R.id.cartItemDates)
        val price: TextView = view.findViewById(R.id.cartItemPrice)
        val remove: ImageButton = view.findViewById(R.id.cartRemoveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val booking = bookings[position]
        holder.image.setImageResource(booking.product.imageRes)
        holder.name.text = booking.product.name
        holder.size.text = "Size: ${booking.size}"
        holder.dates.text = "${booking.formattedPickup}  →  ${booking.formattedReturn}  (${booking.rentalDays} day${if (booking.rentalDays != 1) "s" else ""})"
        holder.price.text = booking.formattedSubtotal
        holder.remove.setOnClickListener { onRemove(booking.product.id) }
    }

    override fun getItemCount() = bookings.size
}