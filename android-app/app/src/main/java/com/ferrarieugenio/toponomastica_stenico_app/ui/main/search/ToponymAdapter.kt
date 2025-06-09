package com.ferrarieugenio.toponomastica_stenico_app.ui.main.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.util.ToponymDiffCallback

class ToponymAdapter(
    private var items: List<Toponym>,
    private val onClick: (Toponym) -> Unit
) : RecyclerView.Adapter<ToponymAdapter.ToponymViewHolder>() {

    inner class ToponymViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.toponymName)

        fun bind(toponym: Toponym) {
            nameTextView.text = toponym.nome

            itemView.setOnClickListener {
                onClick(toponym)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToponymViewHolder {    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_toponym, parent, false)  // <-- here you specify the item layout XML
        return ToponymViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ToponymViewHolder, position: Int) {
        val toponym = items[position]
        holder.nameTextView.text = toponym.nome
        holder.bind(toponym)
        holder.itemView.setOnClickListener {
            onClick(toponym)
        }
    }

    fun updateList(newItems: List<Toponym>) {
        val diffCallback = ToponymDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }
}