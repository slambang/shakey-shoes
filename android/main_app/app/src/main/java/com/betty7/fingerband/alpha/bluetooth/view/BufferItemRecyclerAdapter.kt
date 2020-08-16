package com.betty7.fingerband.alpha.bluetooth.view

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.betty7.fingerband.alpha.bluetooth.view.bufferitem.BufferItemView
import com.betty7.fingerband.alpha.bluetooth.view.bufferitem.BufferItemViewListener

class BufferItemRecyclerAdapter(
    private val listener: BufferItemViewListener
) : RecyclerView.Adapter<BufferItemRecyclerAdapter.ViewHolder>() {

    private lateinit var recycler: RecyclerView

    private val items = mutableListOf<RcbItemModel>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recycler = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = BufferItemView(parent.context).apply {

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            setListener(listener)
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    fun updateItem(item: RcbItemModel) {
        val index = requireIndex(item.id, false)
        if (index == -1) {
            items.add(item)
            notifyItemInserted(items.size - 1)
        } else {
            items[index] = item
            getViewHolder(index)?.bind(item)
        }
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    fun deleteItem(bufferId: Int) {
        val index = requireIndex(bufferId, true)
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun setPage(bufferId: Int, page: Int) {
        val index = requireIndex(bufferId, false)
        if (index != -1) {
            getViewHolder(index)?.setPage(page)
        }
    }

    private fun requireIndex(id: Int, throwIfMissing: Boolean): Int {
        val index = items.indexOfFirst { it.id == id }
        if (index == -1 && throwIfMissing) throw IllegalArgumentException("Required index: $id")
        return index
    }

    private fun getViewHolder(index: Int) =
        (recycler.findViewHolderForAdapterPosition(index) as ViewHolder?)

    override fun getItemCount() = items.size

    inner class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val circularBufferView = (itemView as BufferItemView)

        fun bind(model: RcbItemModel) = circularBufferView.bind(model)
        fun setPage(index: Int) = circularBufferView.setPageIndex(index)
    }
}
