package com.brahmware.lumi_alpha

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ProductAdapter(
    private var products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView      = view as MaterialCardView
        val image: ImageView            = view.findViewById(R.id.productImage)
        val name: TextView              = view.findViewById(R.id.productName)
        val price: TextView             = view.findViewById(R.id.productPrice)
        val brand: TextView             = view.findViewById(R.id.productBrand)
        val viewDetails: MaterialButton = view.findViewById(R.id.viewDetailsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.image.setImageResource(product.imageRes)
        holder.name.text  = product.name
        holder.price.text = product.formattedPrice
        holder.brand.text = product.brand
        holder.viewDetails.setOnClickListener { onProductClick(product) }
        holder.card.setOnClickListener { onProductClick(product) }

        val heartBtn = holder.card.findViewById<ImageButton>(R.id.wishlistToggle)
        updateHeartIcon(heartBtn, WishlistManager.isWishlisted(product.id))
        heartBtn.setOnClickListener {
            val nowWishlisted = WishlistManager.toggle(product)
            updateHeartIcon(heartBtn, nowWishlisted)
        }
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    private fun updateHeartIcon(btn: ImageButton, wishlisted: Boolean) {
        btn.setImageResource(
            if (wishlisted) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
        btn.imageTintList = ColorStateList.valueOf(
            Color.parseColor(if (wishlisted) "#C084FC" else "#FFFFFF")
        )
    }
}