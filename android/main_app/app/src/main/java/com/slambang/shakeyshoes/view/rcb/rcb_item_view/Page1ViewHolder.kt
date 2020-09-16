package com.slambang.shakeyshoes.view.rcb.rcb_item_view

import android.view.View
import android.widget.TextView
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.view.rcb.Page1Model
import com.slambang.shakeyshoes.view.rcb.PageModel

class Page1ViewHolder internal constructor(
    itemView: View,
    listener: BufferItemViewListener
) : RcbItemViewAdapter.BaseViewHolder(itemView) {

    private lateinit var model: Page1Model

    private val productUrl: View = itemView.findViewById(R.id.page_1_product_url)
    private val status: TextView = itemView.findViewById(R.id.page_1_status)
    private val macAddress: TextView = itemView.findViewById(R.id.page_1_mac_address)
    private val pin: TextView = itemView.findViewById(R.id.page_1_pin)
    private val baud: TextView = itemView.findViewById(R.id.page_1_baud)
    private val connectButton: TextView = itemView.findViewById(R.id.page_1_connect_button)

    init {
        productUrl.setOnClickListener {
            listener.onProductUrlClicked(model.modelId)
        }

        connectButton.setOnClickListener {
            listener.onConnectClicked(model.modelId)
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
        const val LAYOUT_RES_ID = R.layout.fragment_rcb_device_page_1
    }
}
