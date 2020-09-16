package com.slambang.shakeyshoes.view.rcb.rcb_item_view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.view.rcb.Page2Model
import com.slambang.shakeyshoes.view.rcb.PageModel

class Page2ViewHolder internal constructor(
    itemView: View,
    listener: BufferItemViewListener
) : RcbItemViewAdapter.BaseViewHolder(itemView) {

    private lateinit var model: Page2Model

    private val editButton: ImageView = itemView.findViewById(R.id.page_2_edit_button)
    private val maxSize: TextView = itemView.findViewById(R.id.page_2_max_bytes)
    private val actualSize: TextView = itemView.findViewById(R.id.page_2_calculated_size)
    private val latency: TextView = itemView.findViewById(R.id.page_2_latency)
    private val underflowTime: TextView = itemView.findViewById(R.id.page_2_underflow_time)
    private val refillCount: TextView = itemView.findViewById(R.id.page_2_refill_count)
    private val refillSize: TextView = itemView.findViewById(R.id.page_2_refill_size)
    private val windowSize: TextView = itemView.findViewById(R.id.page_2_window_size)
    private val maxUnderflows: TextView = itemView.findViewById(R.id.page_2_max_underflows)
    private val applyButton: View = itemView.findViewById(R.id.page_2_apply_button)

    init {
        editButton.setOnClickListener {
            listener.onEditConfigClicked(model.modelId)
        }

        applyButton.setOnClickListener {
            listener.onApplyClicked(model.modelId)
        }
    }

    override fun bind(model: PageModel) {
        this.model = model as Page2Model

        maxSize.text = model.config.maxSize
        actualSize.text = model.config.actualSize
        latency.text = model.config.latency
        underflowTime.text = model.config.maxUnderflowTime

        refillCount.text = model.config.refillCount.toString()
        refillSize.text = model.config.refillSize.toString()
        windowSize.text = model.config.windowSize.toString()
        maxUnderflows.text = model.config.maxUnderflows.toString()

        listOf(applyButton, editButton)
            .forEach {
                it.isEnabled = model.applyButtonEnabled
            }

        editButton.isEnabled = model.applyButtonEnabled
    }

    companion object {
        const val LAYOUT_RES_ID = R.layout.fragment_rcb_device_page_2
    }
}
