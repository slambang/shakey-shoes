package com.slambang.shakeyshoes.view.rcb.rcb_item_view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.view.rcb.ActivePageModel
import com.slambang.shakeyshoes.view.rcb.ItemHeaderModel
import com.slambang.shakeyshoes.view.rcb.RcbItemModel
import ru.tinkoff.scrollingpagerindicator.ScrollingPagerIndicator

interface BufferItemViewListener {
    fun onPauseRcbClicked(modelId: Int)
    fun onResumeClicked(modelId: Int)

    fun onConnectClicked(modelId: Int)
    fun onSetVibrateValue(modelId: Int, value: Int)
    fun onApplyClicked(modelId: Int)
    fun onProductUrlClicked(modelId: Int)
    fun onDeleteClicked(modelId: Int)
    fun onEditConfigClicked(modelId: Int)
}

class BufferItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var pageIndexUpdatedSince = 0L
    private val pagerSnapHelper = PagerSnapHelper()
    private val recyclerAdapter = RcbItemViewAdapter(context)

    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceName: TextView
    private lateinit var connectedIcon: ImageView
    private lateinit var scrollIndicator: ScrollingPagerIndicator
    private lateinit var deleteButton: ImageView

    init {
        inflate(context, R.layout.rcb_item_view, this)
        setupView()
    }

    private lateinit var model: RcbItemModel
    private lateinit var listener: BufferItemViewListener

    private val connectingAnimation = AlphaAnimation(1.0f, 0.0f).apply {
        duration = 350
        repeatMode = Animation.REVERSE
        repeatCount = Animation.INFINITE
    }

    fun setListener(listener: BufferItemViewListener) {
        this.listener = listener
        recyclerAdapter.listener = listener
    }

    fun bind(model: RcbItemModel) {
        this.model = model
        updateHeader(model.header)
        updatePageIndex(model.activePage)
        recyclerAdapter.updatePages(model.page1, model.page2, model.page3)
    }

    private fun updatePageIndex(pageModel: ActivePageModel) {
        if (pageModel.since > pageIndexUpdatedSince) {
            pageIndexUpdatedSince = pageModel.since
            recyclerView.smoothScrollToPosition(pageModel.pageIndex)
        }
    }

    private fun updateHeader(headerModel: ItemHeaderModel) {

        deviceName.text = headerModel.deviceName
        connectedIcon.isEnabled = headerModel.isConnected

        if (headerModel.isConnecting) {
            connectedIcon.startAnimation(connectingAnimation)
        } else {
            connectedIcon.clearAnimation()
        }
    }

    private fun setupView() {

        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        recyclerView = findViewById(R.id.buffer_view_recycler)
        deviceName = findViewById(R.id.buffer_view_header_device_name)
        connectedIcon = findViewById(R.id.buffer_view_header_connected_icon)
        scrollIndicator = findViewById(R.id.buffer_view_indicator)
        deleteButton = findViewById(R.id.buffer_view_delete_button)

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            addOnScrollListener(SimpleRecyclerScrollListener {
                hideKeyboard()
            })
        }

        pagerSnapHelper.attachToRecyclerView(recyclerView)
        recyclerView.setItemViewCacheSize(3) // TODO: This isn't behaving as we expect
        recyclerView.adapter = recyclerAdapter
        scrollIndicator.attachToRecyclerView(recyclerView)

        deleteButton.setOnClickListener {
            listener.onDeleteClicked(model.id)
        }
    }

    private fun hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
