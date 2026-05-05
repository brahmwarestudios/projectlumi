package com.brahmware.lumi_alpha

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class GownAdapter(
    private val gownList: List<Int>,
    private val onGownSelected: (Int) -> Unit
) : RecyclerView.Adapter<GownAdapter.GownViewHolder>() {

    inner class GownViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gownThumbnail: ImageView = view.findViewById(R.id.gownThumbnail)

        fun bind(gownResourceId: Int) {
            gownThumbnail.setImageResource(gownResourceId)
            itemView.setOnClickListener {
                onGownSelected(gownResourceId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GownViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gown, parent, false)
        return GownViewHolder(view)
    }

    override fun onBindViewHolder(holder: GownViewHolder, position: Int) {
        holder.bind(gownList[position])
    }

    override fun getItemCount(): Int = gownList.size
}