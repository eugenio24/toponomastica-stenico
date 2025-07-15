package com.ferrarieugenio.toponomastica_stenico_app.ui.adapters

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.util.filters.SortField
import com.ferrarieugenio.toponomastica_stenico_app.util.filters.SortOption
import com.ferrarieugenio.toponomastica_stenico_app.util.recyclerview.ToponymDiffCallback
import com.l4digital.fastscroll.FastScroller

class ToponymAdapter(
    private var items: List<Toponym>,
    private val onClick: (Toponym) -> Unit
) : RecyclerView.Adapter<ToponymAdapter.ToponymViewHolder>(),
    FastScroller.SectionIndexer {

    private var currentSortOption: SortOption = SortOption()
    private var currentQuery: String = ""

    inner class ToponymViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.toponymName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.toponymDescription)

        fun bind(toponym: Toponym, query: String) {
            nameTextView.text = toponym.nome

            descriptionTextView.visibility = View.VISIBLE
            descriptionTextView.text = if (query.isBlank()) {
                if (toponym.descrizione.length > 80) toponym.descrizione.substring(0, 60) + "…" else toponym.descrizione
            } else {
                highlightQueryPreview(toponym.descrizione, query)
            }

            itemView.setOnClickListener {
                onClick(toponym)
            }
        }

        private fun highlightQueryPreview(text: String, query: String, snippetLength: Int = 60): CharSequence {
            val textLower = text.lowercase()
            val queryLower = query.lowercase()

            val startIndex = textLower.indexOf(queryLower)
            if (startIndex == -1) {
                val snippet = if (text.length > snippetLength) text.substring(0, snippetLength) + "…" else text
                return SpannableString(snippet)
            }

            val halfSnippet = snippetLength / 2
            val snippetStart = (startIndex - halfSnippet).coerceAtLeast(0)
            val snippetEnd = (startIndex + query.length + halfSnippet).coerceAtMost(text.length)

            var snippet = text.substring(snippetStart, snippetEnd)

            if (snippetStart > 0) snippet = "…$snippet"
            if (snippetEnd < text.length) snippet = "$snippet…"

            val spannable = SpannableString(snippet)
            val snippetLower = snippet.lowercase()
            var matchStart = snippetLower.indexOf(queryLower)
            while (matchStart >= 0) {
                val matchEnd = matchStart + query.length
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    matchStart,
                    matchEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                matchStart = snippetLower.indexOf(queryLower, matchEnd)
            }

            return spannable
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
        holder.bind(toponym, currentQuery)
        holder.itemView.setOnClickListener {
            onClick(toponym)
        }
    }

    fun setSortOption(sortOption: SortOption) {
        currentSortOption = sortOption
        notifyDataSetChanged()
    }

    fun setQuery(query: String) {
        currentQuery = query
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