package com.betty7.fingerband.alpha.bluetooth.view.bufferitem

import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams

open class SeekBarValueObserver(private val listener: (Int) -> Unit) : OnSeekChangeListener {
    override fun onSeeking(seekParams: SeekParams) = listener(seekParams.progress)
    override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
    override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {}
}
