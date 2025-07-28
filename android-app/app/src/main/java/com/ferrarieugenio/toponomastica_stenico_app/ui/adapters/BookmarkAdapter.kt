package com.ferrarieugenio.toponomastica_stenico_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.databinding.ItemBookmarkBinding
import com.ferrarieugenio.toponomastica_stenico_app.util.recyclerview.ToponymDiffCallback

class BookmarkAdapter(
    private var toponyms: List<Toponym> = emptyList(),
    private val onToggleBookmark: (Int) -> Unit,
    private val onOpenDetails: (Int) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    inner class BookmarkViewHolder(val binding: ItemBookmarkBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val toponym = toponyms[position]
        with(holder.binding) {
            bookmarkName.text = toponym.nome
            removeButton.setOnClickListener { onToggleBookmark(toponym.id) }
            detailButton.setOnClickListener { onOpenDetails(toponym.id) }
        }
    }

    override fun getItemCount(): Int = toponyms.size

    fun updateData(newToponyms: List<Toponym>) {
        val diffCallback = ToponymDiffCallback(this.toponyms, newToponyms)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        toponyms = newToponyms
        diffResult.dispatchUpdatesTo(this)
    }
}