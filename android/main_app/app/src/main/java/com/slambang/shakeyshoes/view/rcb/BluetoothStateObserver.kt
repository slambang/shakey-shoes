package com.slambang.shakeyshoes.view.rcb

import com.slambang.shakeyshoes.domain.ObservableBluetoothStatus
import com.slambang.shakeyshoes.view.rcb.mappers.BluetoothMessageMapper
import io.reactivex.Observable
import javax.inject.Inject

class BluetoothStateObserver @Inject constructor(
    private val bluetoothStatus: ObservableBluetoothStatus,
    private val bluetoothMessageMapper: BluetoothMessageMapper
) {

    fun observeBluetoothStatus(): Observable<String> =
        bluetoothStatus.subscribe()
            .map {
                bluetoothMessageMapper.map(it)
            }
}
