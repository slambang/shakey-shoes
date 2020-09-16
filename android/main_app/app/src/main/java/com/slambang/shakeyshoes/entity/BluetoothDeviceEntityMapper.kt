package com.slambang.shakeyshoes.entity

import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import javax.inject.Inject

class BluetoothDeviceEntityMapper @Inject constructor() {

    fun map(entities: List<BluetoothDeviceEntity>): List<BluetoothDeviceDomain> =
        entities.map { map(it) }

    fun map(entity: BluetoothDeviceEntity): BluetoothDeviceDomain {

        val productMap = requireProduct(entity.id)

        return BluetoothDeviceDomain(
            entity.id,
            productMap.first,
            entity.macAddress,
            entity.pairingPin,
            entity.serviceUuid,
            productMap.second,
            entity.baudRateBits / 10, // [1 start-bit, 8 data-bits, 1 stop-bit]
        )
    }

    private fun requireProduct(id: Int) =
        PRODUCT_MAPPINGS[id] ?: throw IllegalArgumentException("Required product mapping $id")

    companion object {
        private val PRODUCT_MAPPINGS = mapOf(
            BluetoothDeviceEntity.Hc05DsdMk0.id to Pair(
                "HC-05 (DSD): Mk0",
                "https://www.amazon.co.uk/gp/product/B01G9KSAF6/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1"
            ),
            BluetoothDeviceEntity.Hc05DsdMk1.id to Pair(
                "HC-05 (DSD): Mk1",
                "https://www.amazon.co.uk/gp/product/B01G9KSAF6/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1"
            ),
            BluetoothDeviceEntity.Hc05Wingoneer.id to Pair(
                "HC-05 (Wingoneer)",
                "https://www.amazon.co.uk/gp/product/B01DC9FZ2I/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1"
            )
        )
    }
}
