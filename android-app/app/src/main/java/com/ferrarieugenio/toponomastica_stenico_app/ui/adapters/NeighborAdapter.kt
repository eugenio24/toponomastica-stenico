package com.ferrarieugenio.toponomastica_stenico_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.databinding.ItemNeighborBinding

class NeighborAdapter(
    private val items: List<Toponym>,
    private val onClick: (Toponym) -> Unit = {}
) : RecyclerView.Adapter<NeighborAdapter.NeighborViewHolder>() {

    inner class NeighborViewHolder(val binding: ItemNeighborBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NeighborViewHolder {
        val binding = ItemNeighborBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NeighborViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NeighborViewHolder, position: Int) {
        val toponym = items[position]
        holder.binding.apply {
            neighborNameTextView.text = toponym.nome
            neighborInfoTextView.text = "Quota: ${toponym.quota} m"
            root.setOnClickListener { onClick(toponym) }
        }
    }

    override fun getItemCount(): Int = items.size
}