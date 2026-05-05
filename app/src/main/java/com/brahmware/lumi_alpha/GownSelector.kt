package com.brahmware.lumi_alpha

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class GownSelector(
    private val context: Context,
    private val container: LinearLayout,
    private val categories: List<Category>,
    private val onGownSelected: (GownItem) -> Unit
) {
    private val cards = mutableListOf<MaterialCardView>()
    private var selectedCard: MaterialCardView? = null

    enum class ItemType {
        GOWN,
        NECKLACE,
        MALE_OUTFIT
    }

    data class GownItem(
        val drawableRes: Int,
        val name: String,
        val type: ItemType = ItemType.GOWN
    )

    data class Category(
        val title: String,
        val items: List<GownItem>
    )

    init {
        buildSelector()
    }

    private fun buildSelector() {
        categories.forEach { category ->
            // Category title
            val titleView = TextView(context).apply {
                text = category.title
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#C084FC"))
                setPadding(24, 16, 0, 8)
            }
            container.addView(titleView)

            // Horizontal row for this category
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            category.items.forEach { item ->
                val cardView = LayoutInflater.from(context)
                    .inflate(R.layout.item_gown_card, rowLayout, false)

                val thumbnail = cardView.findViewById<ImageView>(R.id.gownThumbnail)
                val nameText = cardView.findViewById<TextView>(R.id.gownName)
                val card = cardView as MaterialCardView

                thumbnail.setImageResource(item.drawableRes)

                // Hide the name label
                nameText.visibility = TextView.GONE

                card.setOnClickListener {
                    selectCard(card)
                    onGownSelected(item)
                }

                cards.add(card)
                rowLayout.addView(cardView)
            }

            container.addView(rowLayout)
        }

        // Select first card by default
        if (cards.isNotEmpty()) {
            selectCard(cards[0])
        }
    }

    private fun selectCard(card: MaterialCardView) {
        selectedCard?.strokeWidth = 0
        selectedCard?.cardElevation = 4f
        card.strokeWidth = 4
        card.cardElevation = 8f
        selectedCard = card
    }
}