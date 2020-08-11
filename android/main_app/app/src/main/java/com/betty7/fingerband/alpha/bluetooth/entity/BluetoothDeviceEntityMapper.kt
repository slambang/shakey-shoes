package com.betty7.fingerband.alpha.bluetooth.entity

import com.betty7.fingerband.alpha.bluetooth.domain.CircularBufferStatus
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceDomain

class BluetoothDeviceEntityMapper {

    fun map(entities: List<BluetoothDeviceEntity>): List<DeviceDomain> =
        entities.map { map(it) }

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
            CircularBufferStatus.READY
        )
    }

    private fun requireProduct(id: Int) =
        PRODUCT_MAPPINGS[id] ?: throw IllegalArgumentException("Required product mapping $id")

    companion object {
        private val PRODUCT_MAPPINGS = mapOf(
            HC05_DSD_MK1.id to Pair(
                "HC-05 (DSD): MK1",
                "https://www.amazon.co.uk/gp/product/B01DC9FZ2I/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1"
            ),
            HC05_DSD.id to Pair(
                "HC-05 (DSD)",
                "https://www.amazon.co.uk/gp/product/B01G9KSAF6/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1"
            ),
            HC05_WINGONEER.id to Pair(
                "HC-05 (Wingoneer)",
                "https://www.amazon.co.uk/gp/product/B01DC9FZ2I/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1"
            )
        )
    }
}
