package com.slambang.shakeyshoes.view.rcb.rcb_item_view

import android.view.View
import android.widget.TextView
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.view.rcb.Page3Model
import com.slambang.shakeyshoes.view.rcb.PageModel
import com.warkiz.widget.IndicatorSeekBar

class Page3ViewHolder internal constructor(
    itemView: View,
    listener: BufferItemViewListener
) : RcbItemViewAdapter.BaseViewHolder(itemView) {

    private lateinit var model: Page3Model

    private val upTime: Chronometer = itemView.findViewById(R.id.page_3_up_time)
    private val successRate: TextView = itemView.findViewById(R.id.page_3_success_rate)
    private val errorRate: TextView = itemView.findViewById(R.id.page_3_error_rate)
    private val resumeButton: TextView = itemView.findViewById(R.id.page_3_resume_button)
    private val vibrateValue: IndicatorSeekBar = itemView.findViewById(R.id.page_3_vibrate_value)

    init {
        resumeButton.setOnClickListener {
            listener.onResumeClicked(model.id)
        }

        vibrateValue.onSeekChangeListener = SeekBarValueObserver {
            listener.onSetVibrateValue(model.id, it)
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
        const val LAYOUT_RES_ID = R.layout.fragment_rcb_device_page_3
    }
}
