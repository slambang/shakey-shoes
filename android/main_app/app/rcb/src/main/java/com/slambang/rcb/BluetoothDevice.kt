package com.slambang.rcb

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.Executors

class BluetoothDevice private constructor(
    private val scheduler: Scheduler,
    private val bluetoothProvider: BluetoothProvider,
    private val subscriptions: CompositeDisposable
) : BluetoothConnection {

    private var bluetoothSocket: BluetoothSocket? = null

    override val isConnected: Boolean
        get() = bluetoothSocket?.isConnected ?: false

    override val inputStream: InputStream
        get() = requireBluetoothSocket().inputStream

    override val outputStream: OutputStream
        get() = requireBluetoothSocket().outputStream

    override fun start(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    ) {
        scheduler.scheduleDirect {
            stateObserver(BluetoothConnectionState.CONNECTING)
            if (checkBluetoothCapabilities(stateObserver)) {
                observeBluetoothStates(stateObserver)
                connectToRemoteDevice(macAddress, serviceUuid, stateObserver)
            }
        }.also { subscriptions.add(it) }
    }

    override fun stop() {
        try {
            if (isConnected) {
                requireBluetoothSocket().close()
                bluetoothSocket = null
            }
        } finally {
            subscriptions.clear()
        }
    }

    private fun checkBluetoothCapabilities(stateObserver: (state: BluetoothConnectionState) -> Unit) =
        if (!bluetoothProvider.isBluetoothAvailable) {
            stateObserver(BluetoothConnectionState.UNAVAILABLE)
            false
        } else if (!bluetoothProvider.isBluetoothEnabled) {
            stateObserver(BluetoothConnectionState.DISABLED)
            false
        } else {
            true
        }

    private fun connectToRemoteDevice(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    ) = bluetoothProvider.bondedDevices?.firstOrNull {
        it.address == macAddress
    }?.let {
        createSocket(serviceUuid, it, stateObserver)
    } ?: stateObserver(BluetoothConnectionState.CONNECTION_ERROR)

    private fun observeBluetoothStates(stateObserver: (state: BluetoothConnectionState) -> Unit) =
        bluetoothProvider.bluetoothState
            .subscribe({
                when (it) {
                    BluetoothAdapter.STATE_TURNING_OFF -> stateObserver(BluetoothConnectionState.DISABLED)
                    else -> {
                    }
                }
            },
                {
                    it.printStackTrace()
                    stateObserver(BluetoothConnectionState.GENERIC_ERROR)
                })
            .also { subscriptions.add(it) }

    private fun createSocket(
        serviceUuid: String,
        device: BluetoothDevice,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    ) = try {
        val uuid = UUID.fromString(serviceUuid)
        bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
        bluetoothSocket?.connect()

        stateObserver(BluetoothConnectionState.CONNECTED)
    } catch (error: Throwable) {
        error.printStackTrace()
        stateObserver(BluetoothConnectionState.GENERIC_ERROR)
    }

    private fun requireBluetoothSocket() =
        bluetoothSocket ?: throw IllegalStateException("Required bluetooth socket")

    companion object {
        fun newInstance(context: Context): com.slambang.rcb.BluetoothDevice {
            val subscriptions = CompositeDisposable()
            val rxBluetooth = RxBluetooth(context)
            val provider = BluetoothProviderImpl(rxBluetooth)
            val scheduler = Schedulers.from(Executors.newSingleThreadExecutor())
            return BluetoothDevice(
                scheduler,
                provider,
                subscriptions
            )
        }
    }
}
