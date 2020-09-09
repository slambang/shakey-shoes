package com.slambang.shakeyshoes.view.rcb

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.slambang.shakeyshoes.view.rcb.rcb_item_view.BufferItemView
import com.slambang.shakeyshoes.view.rcb.rcb_item_view.BufferItemViewListener
import javax.inject.Inject

class BufferItemRecyclerAdapter @Inject constructor(
    private val listener: BufferItemViewListener
) : RecyclerView.Adapter<BufferItemRecyclerAdapter.ViewHolder>() {

    private lateinit var recycler: RecyclerView

    private val items = mutableListOf<RcbItemModel>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recycler = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = getView(parent)
        return ViewHolder(view)
    }

    private fun getView(parent: ViewGroup): BufferItemView =
        BufferItemView(parent.context).apply {
            setListener(listener)
        }

    fun updateItem(item: RcbItemModel, index: Int) =
        if (index == items.size) {
            items.add(item)
            notifyItemInserted(index)
        } else {
            items[index] = item
            notifyItemChanged(index, item)
        }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    fun removeItem(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    // TODO Update model instead. Use timestamps for setting page!
    fun setPage(index: Int, page: Int) =
        getViewHolder(index)?.setPage(page)

    private fun getViewHolder(index: Int) =
        (recycler.findViewHolderForAdapterPosition(index) as ViewHolder?)

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) =
        if (payloads.isNotEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    class ViewHolder internal constructor(itemView: BufferItemView) :
        RecyclerView.ViewHolder(itemView) {

        private val circularBufferView = itemView

        fun bind(model: RcbItemModel) = circularBufferView.bind(model)
        fun setPage(index: Int) = circularBufferView.setPageIndex(index)
    }
}
