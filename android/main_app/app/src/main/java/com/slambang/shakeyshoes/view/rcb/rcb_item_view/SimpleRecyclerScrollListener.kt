package com.slambang.shakeyshoes.view.rcb.rcb_item_view

import androidx.recyclerview.widget.RecyclerView

class SimpleRecyclerScrollListener(private val callback: () -> Unit) :
    RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) callback()
    }
}
