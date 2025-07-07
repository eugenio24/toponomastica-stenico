package com.ferrarieugenio.toponomastica_stenico_app.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.util.SortField
import com.ferrarieugenio.toponomastica_stenico_app.util.SortOption
import com.ferrarieugenio.toponomastica_stenico_app.util.ToponymDiffCallback
import com.l4digital.fastscroll.FastScroller

class ToponymAdapter(
    private var items: List<Toponym>,
    private val onClick: (Toponym) -> Unit
) : RecyclerView.Adapter<ToponymAdapter.ToponymViewHolder>(),
    FastScroller.SectionIndexer {

    private var currentSortOption: SortOption = SortOption()

    inner class ToponymViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.toponymName)

        fun bind(toponym: Toponym) {
            nameTextView.text = toponym.nome

            itemView.setOnClickListener {
                onClick(toponym)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToponymViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_toponym,
            parent,
            false
        )
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

    fun setSortOption(sortOption: SortOption) {
        currentSortOption = sortOption
        notifyDataSetChanged()
    }

    override fun getSectionText(position: Int): CharSequence {
        return when (currentSortOption.field) {
            SortField.NAME -> items[position].nome.first().uppercaseChar().toString()
            SortField.QUOTA -> {
                val quota = items[position].quota
                val rangeStart = (quota / 100) * 100
                val rangeEnd = rangeStart + 99
                "$rangeStart-$rangeEnd"
            }
        }
    }

    fun updateList(newItems: List<Toponym>) {
        val diffCallback = ToponymDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }
}