package com.betty7.fingerband.alpha.bluetooth.view.bufferitem

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.betty7.fingerband.alpha.bluetooth.view.Page0ViewModel
import com.betty7.fingerband.alpha.bluetooth.view.Page1ViewModel
import com.betty7.fingerband.alpha.bluetooth.view.Page2ViewModel
import com.betty7.fingerband.alpha.bluetooth.view.PageModel

class BufferItemViewAdapter(context: Context) :
    RecyclerView.Adapter<BufferItemViewAdapter.BaseViewHolder>() {

    lateinit var onProductUrlClicked: () -> Unit
    lateinit var onConnectClicked: () -> Unit
    lateinit var onApplyClicked: () -> Unit
    lateinit var onResumeClicked: () -> Unit
    lateinit var onVibrateUpdate: (Int) -> Unit
    lateinit var onEditConfig: () -> Unit

    private val items = mutableListOf<PageModel>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        with(inflater.inflate(PAGE_LAYOUTS[viewType], parent, false)) {
            when (viewType) {
                0 -> Page0ViewHolder(this, onProductUrlClicked, onConnectClicked)
                1 -> Page1ViewHolder(this, onApplyClicked, onEditConfig)
                2 -> Page2ViewHolder(this, onResumeClicked, onVibrateUpdate)
                else -> throw IllegalStateException("Invalid viewType: $viewType")
            }
        }

    // Optimisation: It would be nice if we could only receive the pages that need updating
    fun updatePages(page0: Page0ViewModel, page1: Page1ViewModel, page2: Page2ViewModel) {
        items.addAll(listOf(page0, page1, page2))
        bindWithoutNotify(page0, page1, page2)
    }

    /*
     * This allows the touch event to work on page 2 when dynamic data is continually coming in.
     * Calls to [notifyDataSetChanged] mean the touch event is lost with dynamic data.
     * This only works from the *second* time that items are updated.
     */
    private fun bindWithoutNotify(
        page0: Page0ViewModel,
        page1: Page1ViewModel,
        page2: Page2ViewModel
    ) = with(recyclerView) {
        (findViewHolderForAdapterPosition(0) as Page0ViewHolder?)?.bind(page0)
        (findViewHolderForAdapterPosition(1) as Page1ViewHolder?)?.bind(page1)
        (findViewHolderForAdapterPosition(2) as Page2ViewHolder?)?.bind(page2)
    }

    override fun getItemCount() = PAGE_LAYOUTS.size

    override fun getItemViewType(position: Int) = position

    // This is only called the very *first* time that the adapter creates ViewHolders
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) =
        holder.bind(items[position])

    abstract class BaseViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        abstract fun bind(model: PageModel)
    }

    companion object {
        private val PAGE_LAYOUTS = listOf(
            Page0ViewHolder.layoutRes,
            Page1ViewHolder.layoutRes,
            Page2ViewHolder.layoutRes
        )
    }
}
