package com.betty7.fingerband.alpha.bluetooth.view.bufferitem

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.betty7.fingerband.alpha.R
import com.betty7.fingerband.alpha.bluetooth.view.Page1ViewModel
import com.betty7.fingerband.alpha.bluetooth.view.PageModel

class Page1ViewHolder internal constructor(
    itemView: View,
    onApplyClicked: () -> Unit,
    onEditConfig: () -> Unit
) : BufferItemViewAdapter.BaseViewHolder(itemView) {

    private val editButton: ImageView = itemView.findViewById(R.id.page_1_edit_button)
    private val maxSize: TextView = itemView.findViewById(R.id.page_1_max_bytes)
    private val actualSize: TextView = itemView.findViewById(R.id.page_1_calculated_size)
    private val latency: TextView = itemView.findViewById(R.id.page_1_latency)
    private val underflowTime: TextView = itemView.findViewById(R.id.page_1_underflow_time)
    private val refillCount: TextView = itemView.findViewById(R.id.page_1_refill_count)
    private val refillSize: TextView = itemView.findViewById(R.id.page_1_refill_size)
    private val windowSize: TextView = itemView.findViewById(R.id.page_1_window_size)
    private val maxUnderflows: TextView = itemView.findViewById(R.id.page_1_max_underflows)
    private val applyButton: View = itemView.findViewById(R.id.page_1_apply_button)

    init {
        editButton.setOnClickListener {
            onEditConfig()
        }

        applyButton.setOnClickListener {
            onApplyClicked()
        }
    }

    override fun bind(model: PageModel) {
        model as Page1ViewModel

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

        when (model.applyButtonEnabled) {
            true -> R.drawable.ic_pencil
            false -> R.drawable.ic_pencil_grey
        }.let {
            editButton.setImageResource(it)
        }
    }

    companion object {
        const val layoutRes = R.layout.circular_buffer_view_page_1
    }
}
