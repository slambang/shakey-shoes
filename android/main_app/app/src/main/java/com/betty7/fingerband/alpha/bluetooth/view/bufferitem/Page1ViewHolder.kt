package com.betty7.fingerband.alpha.bluetooth.view.bufferitem

import android.view.View
import android.widget.TextView
import com.betty7.fingerband.alpha.R
import com.betty7.fingerband.alpha.bluetooth.view.Page1Model
import com.betty7.fingerband.alpha.bluetooth.view.PageModel

class Page1ViewHolder internal constructor(
    itemView: View,
    listener: BufferItemViewListener
) : BufferItemViewAdapter.BaseViewHolder(itemView) {

    private lateinit var model: Page1Model

    private val productUrl: View = itemView.findViewById(R.id.page_0_product_url)
    private val status: TextView = itemView.findViewById(R.id.page_0_status)
    private val macAddress: TextView = itemView.findViewById(R.id.page_0_mac_address)
    private val pin: TextView = itemView.findViewById(R.id.page_0_pin)
    private val baud: TextView = itemView.findViewById(R.id.page_0_baud)
    private val connectButton: TextView = itemView.findViewById(R.id.page_0_connect_button)

    init {
        productUrl.setOnClickListener {
            listener.onProductUrlClicked(model.id)
        }

        connectButton.setOnClickListener {
            listener.onConnectClicked(model.id)
        }
    }

    override fun bind(model: PageModel) {
        this.model = model as Page1Model

        status.text = model.status
        macAddress.text = model.macAddress
        pin.text = model.pairingPin
        baud.text = model.baudRateBytes
        connectButton.isEnabled = model.connectButtonEnabled
        connectButton.text = model.connectButtonText
    }

    companion object {
        const val layoutRes = R.layout.circular_buffer_view_page_0
    }
}
