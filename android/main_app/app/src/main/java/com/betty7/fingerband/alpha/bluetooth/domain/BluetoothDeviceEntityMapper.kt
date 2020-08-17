package com.betty7.fingerband.alpha.bluetooth.domain

import com.betty7.fingerband.alpha.bluetooth.data.entity.BluetoothDeviceEntity
import com.betty7.fingerband.alpha.bluetooth.data.entity.HC05_DSD_MK0
import com.betty7.fingerband.alpha.bluetooth.data.entity.HC05_DSD_MK1
import com.betty7.fingerband.alpha.bluetooth.data.entity.HC05_WINGONEER

class BluetoothDeviceEntityMapper {

    fun map(entity: BluetoothDeviceEntity): DeviceDomain {

        val productMap = requireProduct(entity.id)

        return DeviceDomain(
            entity.id,
            productMap.first,
            entity.macAddress,
            entity.pairingPin,
            entity.serviceUuid,
            productMap.second,
            entity.baudRateBits / 10, // [1 start-bit, 8 data-bits, 1 stop-bit]
            0,
            RcbServiceState.DISCONNECTED
        )
    }

    private fun requireProduct(id: Int) =
        PRODUCT_MAPPINGS[id] ?: throw IllegalArgumentException("Required product mapping $id")

    companion object {
        private val PRODUCT_MAPPINGS = mapOf(
            HC05_DSD_MK0.id to Pair(
                "HC-05 (DSD): Mk0",
                "https://www.amazon.co.uk/gp/product/B01G9KSAF6/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1"
            ),
            HC05_DSD_MK1.id to Pair(
                "HC-05 (DSD): Mk1",
                "https://www.amazon.co.uk/gp/product/B01G9KSAF6/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1"
            ),
            HC05_WINGONEER.id to Pair(
                "HC-05 (Wingoneer)",
                "https://www.amazon.co.uk/gp/product/B01DC9FZ2I/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1"
            )
        )
    }
}
