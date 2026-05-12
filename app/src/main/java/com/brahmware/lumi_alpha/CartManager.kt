package com.brahmware.lumi_alpha

object CartManager {

    private val _bookings = mutableListOf<RentalBooking>()
    val bookings: List<RentalBooking> get() = _bookings.toList()

    val totalCount: Int get() = _bookings.size
    val totalPrice: Int get() = _bookings.sumOf { it.subtotal }

    fun formattedTotal(): String = "₱${"%,d".format(totalPrice)}"

    fun addBooking(booking: RentalBooking) {
        // One slot per product — replacing avoids duplicate bookings
        _bookings.removeAll { it.product.id == booking.product.id }
        _bookings.add(booking)
    }

    fun removeBooking(productId: Int) {
        _bookings.removeAll { it.product.id == productId }
    }

    fun clear() {
        _bookings.clear()
    }

    fun isEmpty() = _bookings.isEmpty()
}