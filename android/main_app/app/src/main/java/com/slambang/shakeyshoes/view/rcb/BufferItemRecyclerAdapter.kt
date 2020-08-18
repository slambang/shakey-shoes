package com.slambang.shakeyshoes.view.rcb

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.slambang.shakeyshoes.view.rcb.rcb_item_view.BufferItemView
import com.slambang.shakeyshoes.view.rcb.rcb_item_view.BufferItemViewListener

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
        val view = getView(parent)
        return ViewHolder(view)
    }

    private fun getView(parent: ViewGroup) =
        BufferItemView(parent.context).apply {

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            setListener(listener)
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    // This is necessary to keep the main thread from getting clogged
    fun updateItem(item: RcbItemModel, index: Int) {
        if (index >= items.size) {
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

    fun removeItem(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun setPage(index: Int, page: Int) =
        getViewHolder(index)?.setPage(page) // update model instead?

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
