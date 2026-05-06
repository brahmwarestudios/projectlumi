package com.brahmware.lumi_alpha

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val name: String,
    val brand: String,
    val description: String,
    val price: String,
    val imageRes: Int,
    val itemType: GownSelector.ItemType
) : Parcelable