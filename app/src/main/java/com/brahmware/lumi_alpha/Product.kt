package com.brahmware.lumi_alpha

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val name: String,
    val brand: String,
    val description: String,
    val rentalPricePerDay: Int,   // in Philippine Peso
    val imageRes: Int,
    val itemType: GownSelector.ItemType,
    val availableSizes: List<String> = listOf("XS", "S", "M", "L", "XL"),
    val isAvailable: Boolean = true
) : Parcelable {
    val formattedPrice: String get() = "₱${"%,d".format(rentalPricePerDay)}/day"
}
