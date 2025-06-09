package com.ferrarieugenio.toponomastica_stenico_app.util

import androidx.recyclerview.widget.DiffUtil
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym

class ToponymDiffCallback(
    private val oldList: List<Toponym>,
    private val newList: List<Toponym>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}