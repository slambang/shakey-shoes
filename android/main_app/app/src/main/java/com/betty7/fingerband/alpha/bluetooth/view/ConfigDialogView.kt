package com.betty7.fingerband.alpha.bluetooth.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.betty7.fingerband.alpha.R

@SuppressLint("InflateParams")
class ConfigDialogView(
    private val bufferId: Int,
    private val title: String,
    private val context: Context,
    private val applyValuesCallback: (Int, String, String, String, String) -> Unit,
    private val onDismissCallback: () -> Unit
) {
    private val maxSize: TextView
    private val numRefills: TextView
    private val refillSize: TextView
    private val windowSize: TextView
    private val maxUnderflows: TextView

    init {
        with(LayoutInflater.from(context).inflate(R.layout.config_dialog_view, null)) {
            maxSize = findViewById(R.id.page_1_max_bytes)
            numRefills = findViewById(R.id.page_1_refill_count)
            refillSize = findViewById(R.id.page_1_refill_size)
            windowSize = findViewById(R.id.page_1_window_size)
            maxUnderflows = findViewById(R.id.page_1_max_underflows)

            showDialog(this)
        }
    }

    fun bind(configModel: RcbConfigModel) {
        maxSize.text = configModel.maxSize
        numRefills.text = configModel.refillCount.toString()
        refillSize.text = configModel.refillSize.toString()
        windowSize.text = configModel.windowSize.toString()
        maxUnderflows.text = configModel.maxUnderflows.toString()
    }

    private fun showDialog(view: View) =
        AlertDialog.Builder(context)
            .setView(view)
            .setTitle(title)
            .setCancelable(true)
            .setPositiveButton(R.string.apply) { _, _ ->
                applyValues()
            }
            .setOnDismissListener { onDismissCallback() }
            .show()

    private fun applyValues() =
        applyValuesCallback(
            bufferId,
            numRefills.text.toString(),
            refillSize.text.toString(),
            windowSize.text.toString(),
            maxUnderflows.text.toString()
        )
}