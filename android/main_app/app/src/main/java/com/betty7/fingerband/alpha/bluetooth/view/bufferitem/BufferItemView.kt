package com.betty7.fingerband.alpha.bluetooth.view.bufferitem

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.betty7.fingerband.alpha.R
import com.betty7.fingerband.alpha.bluetooth.view.BufferItemHeaderViewModel
import com.betty7.fingerband.alpha.bluetooth.view.BufferItemViewModel
import kotlinx.android.synthetic.main.circular_buffer_view.view.*

class BufferItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    lateinit var onResumeClicked: (Int) -> Unit
    lateinit var onConnectClicked: (Int) -> Unit
    lateinit var onVibrateUpdate: (Int, Int) -> Unit
    lateinit var onApplyClicked: (Int) -> Unit
    lateinit var onProductUrlClicked: (Int) -> Unit
    lateinit var onDeleteClicked: (Int) -> Unit
    lateinit var onEditConfig: (Int) -> Unit

    private var bufferId: Int = -1
    private val pagerSnapHelper = PagerSnapHelper()
    private val recyclerAdapter = BufferItemViewAdapter(context)

    private val connectingAnimation = AlphaAnimation(1.0f, 0.0f).apply {
        duration = 350
        repeatMode = Animation.REVERSE
        repeatCount = Animation.INFINITE
    }

    init {
        inflate(context, R.layout.circular_buffer_view, this)
        bindAdapter()
        setupView()
    }

    fun bind(model: BufferItemViewModel) {
        bufferId = model.id
        updateHeader(model.header)
        recyclerAdapter.updatePages(model.page0, model.page1, model.page2)
    }

    private fun updateHeader(headerModel: BufferItemHeaderViewModel) =
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

    private fun bindAdapter() {
        recyclerAdapter.apply {
            onProductUrlClicked = {
                this@BufferItemView.onProductUrlClicked(bufferId)
            }

            onConnectClicked = {
                this@BufferItemView.onConnectClicked(
                    bufferId
                )
            }

            onApplyClicked = {
                this@BufferItemView.onApplyClicked(bufferId)
            }

            onResumeClicked = {
                this@BufferItemView.onResumeClicked(bufferId)
            }

            onVibrateUpdate = {
                this@BufferItemView.onVibrateUpdate(bufferId, it)
            }

            onEditConfig = {
                this@BufferItemView.onEditConfig(bufferId)
            }
        }.also { buffer_view_recycler.adapter = it }
    }

    private fun setupView() {
        with (buffer_view_recycler) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            addOnScrollListener(SimpleRecyclerScrollListener {
                hideKeyboard()
            })
        }

        pagerSnapHelper.attachToRecyclerView(buffer_view_recycler)
        buffer_view_indicator.attachToRecyclerView(buffer_view_recycler)
        buffer_view_recycler.setItemViewCacheSize(3)

        buffer_view_delete_button.setOnClickListener {
            onDeleteClicked(bufferId)
        }
    }

    fun setPageIndex(index: Int) = buffer_view_recycler.smoothScrollToPosition(index)

    private fun hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
