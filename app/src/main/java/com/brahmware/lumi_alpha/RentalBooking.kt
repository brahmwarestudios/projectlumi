package com.brahmware.lumi_alpha

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class RentalBooking(
    val product: Product,
    val size: String,
    val pickupDate: LocalDate,
    val returnDate: LocalDate
) {
    val rentalDays: Int get() = ChronoUnit.DAYS.between(pickupDate, returnDate).toInt().coerceAtLeast(1)
    val subtotal: Int get() = product.rentalPricePerDay * rentalDays
    val formattedSubtotal: String get() = "₱${"%,d".format(subtotal)}"
    val formattedPickup: String get() = pickupDate.toString()   // replace with DateTimeFormatter if desired
    val formattedReturn: String get() = returnDate.toString()
}