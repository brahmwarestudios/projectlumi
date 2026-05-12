package com.brahmware.lumi_alpha

object WishlistManager {

    private val _items = mutableSetOf<Int>() // stores product IDs

    fun toggle(product: Product): Boolean {
        return if (_items.contains(product.id)) {
            _items.remove(product.id)
            false
        } else {
            _items.add(product.id)
            true
        }
    }

    fun isWishlisted(productId: Int) = _items.contains(productId)

    fun getWishlistedProducts(): List<Product> {
        return ProductRepository.getAll().filter { _items.contains(it.id) }
    }

    fun count() = _items.size
}