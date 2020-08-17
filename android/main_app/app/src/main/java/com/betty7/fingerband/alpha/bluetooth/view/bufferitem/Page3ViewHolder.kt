package com.betty7.fingerband.alpha.bluetooth.view.bufferitem

import android.view.View
import android.widget.TextView
import com.betty7.fingerband.alpha.R
import com.betty7.fingerband.alpha.bluetooth.view.Page3Model
import com.betty7.fingerband.alpha.bluetooth.view.PageModel
import com.warkiz.widget.IndicatorSeekBar

class Page3ViewHolder internal constructor(
    itemView: View,
    listener: BufferItemViewListener
) : BufferItemViewAdapter.BaseViewHolder(itemView) {

    private lateinit var model: Page3Model

    private val upTime: Chronometer = itemView.findViewById(R.id.page_2_up_time)
    private val successRate: TextView = itemView.findViewById(R.id.page_2_success_rate)
    private val errorRate: TextView = itemView.findViewById(R.id.page_2_error_rate)
    private val resumeButton: TextView = itemView.findViewById(R.id.page_2_resume_button)
    private val vibrateValue: IndicatorSeekBar = itemView.findViewById(R.id.page_2_vibrate_value)

    init {
        resumeButton.setOnClickListener {
            listener.onResumeClicked(model.id)
        }

        vibrateValue.onSeekChangeListener = SeekBarValueObserver {
            listener.onVibrateUpdate(model.id, it)
        }
    }

    override fun bind(model: PageModel) {
        this.model = model as Page3Model

        if (model.isResumed) upTime.resume() else upTime.pause()

        vibrateValue.max = model.maxVibrateValue.toFloat()
        vibrateValue.isEnabled = model.resumeButtonEnabled

        resumeButton.text = model.resumeButtonText
        resumeButton.isEnabled = model.resumeButtonEnabled

        successRate.text = model.successRate
        errorRate.text = model.errorRate
    }

    companion object {
        const val layoutRes = R.layout.circular_buffer_view_page_2
    }
}