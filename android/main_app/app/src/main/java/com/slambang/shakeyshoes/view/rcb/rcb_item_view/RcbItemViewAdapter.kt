package com.slambang.shakeyshoes.view.rcb.rcb_item_view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.slambang.shakeyshoes.view.rcb.Page1Model
import com.slambang.shakeyshoes.view.rcb.Page2Model
import com.slambang.shakeyshoes.view.rcb.Page3Model
import com.slambang.shakeyshoes.view.rcb.PageModel

class RcbItemViewAdapter(context: Context) :
    RecyclerView.Adapter<RcbItemViewAdapter.BaseViewHolder>() {

    lateinit var listener: BufferItemViewListener

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
                0 -> Page1ViewHolder(this, listener)
                1 -> Page2ViewHolder(this, listener)
                2 -> Page3ViewHolder(this, listener)
                else -> throw IllegalStateException("Invalid viewType: $viewType")
            }
        }

    // Optimisation: It would be nice if we could only receive the pages that need updating
    fun updatePages(page1: Page1Model, page2: Page2Model, page3: Page3Model) {
        items.addAll(listOf(page1, page2, page3))
        bindWithoutNotify(page1, page2, page3)
    }

    /*
     * This allows the touch event to work on page 3 when dynamic data is continually coming in.
     * Calls to [notifyDataSetChanged] mean the touch event is lost with dynamic data.
     * This only works from the *second+* time that items are updated.
     * TODO: The current setup doesn't appear to be holding onto all ViewHolders!
     *      Reproduce (on an emulator)
     *          1) Click Connect
     *          2) Quickly scroll to page 3
     *          3) Wait for the connection error
     *          4) App scrolls back to page 1
     *          5) Observe page 1 is not updated
     */
    private fun bindWithoutNotify(
        page1: Page1Model,
        page2: Page2Model,
        page3: Page3Model
    ) = with(recyclerView) {
        (findViewHolderForAdapterPosition(0) as Page1ViewHolder?)?.bind(page1)
        (findViewHolderForAdapterPosition(1) as Page2ViewHolder?)?.bind(page2)
        (findViewHolderForAdapterPosition(2) as Page3ViewHolder?)?.bind(page3)
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
            Page1ViewHolder.layoutRes,
            Page2ViewHolder.layoutRes,
            Page3ViewHolder.layoutRes
        )
    }
}
