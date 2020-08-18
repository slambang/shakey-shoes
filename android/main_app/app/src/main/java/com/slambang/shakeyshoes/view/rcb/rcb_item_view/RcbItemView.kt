package com.slambang.shakeyshoes.view.rcb.rcb_item_view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.view.rcb.ItemHeaderModel
import com.slambang.shakeyshoes.view.rcb.RcbItemModel
import kotlinx.android.synthetic.main.rcb_item_view.view.*

interface BufferItemViewListener {
    fun onResumeClicked(modelId: Int)
    fun onConnectClicked(modelId: Int)
    fun onVibrateUpdate(modelId: Int, value: Int)
    fun onApplyClicked(modelId: Int)
    fun onProductUrlClicked(modelId: Int)
    fun onDeleteClicked(modelId: Int)
    fun onEditConfig(modelId: Int)
}

class BufferItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var listener: BufferItemViewListener

    private lateinit var model: RcbItemModel

    private val pagerSnapHelper = PagerSnapHelper()
    private val recyclerAdapter = RcbItemViewAdapter(context)

    private val connectingAnimation = AlphaAnimation(1.0f, 0.0f).apply {
        duration = 350
        repeatMode = Animation.REVERSE
        repeatCount = Animation.INFINITE
    }

    init {
        inflate(context, R.layout.rcb_item_view, this)
        setupView()
    }

    fun setListener(listener: BufferItemViewListener) {
        this.listener = listener
        recyclerAdapter.listener = listener
    }

    fun bind(model: RcbItemModel) {
        this.model = model
        updateHeader(model.header)
        recyclerAdapter.updatePages(model.page1, model.page2, model.page3)
    }

    private fun updateHeader(headerModel: ItemHeaderModel) =
        with(buffer_view_header_container) {

            buffer_view_header_device_name.text = headerModel.deviceName

            if (headerModel.isConnected) {
                buffer_view_header_connected_icon.setImageResource(R.drawable.connection_on)
            } else {
                buffer_view_header_connected_icon.setImageResource(R.drawable.connection_off)
            }

            if (headerModel.isConnecting) {
                buffer_view_header_connected_icon.startAnimation(connectingAnimation)
            } else {
                buffer_view_header_connected_icon.clearAnimation()
            }
        }

    private fun setupView() {
        with(buffer_view_recycler) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            addOnScrollListener(SimpleRecyclerScrollListener {
                hideKeyboard()
            })
        }

        pagerSnapHelper.attachToRecyclerView(buffer_view_recycler)
        buffer_view_recycler.setItemViewCacheSize(3) // TODO: This isn't behaving as we expect
        buffer_view_recycler.adapter = recyclerAdapter
        buffer_view_indicator.attachToRecyclerView(buffer_view_recycler)

        buffer_view_delete_button.setOnClickListener {
            listener.onDeleteClicked(model.id)
        }
    }

    fun setPageIndex(index: Int) = buffer_view_recycler.smoothScrollToPosition(index)

    private fun hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}