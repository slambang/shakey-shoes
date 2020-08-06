package com.betty7.fingerband.alpha.bluetooth.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

abstract class BluetoothPermissionActivity : AppCompatActivity() {

    private lateinit var bluetoothGrantedObserver: () -> Unit
    private lateinit var bluetoothDeniedObserver: () -> Unit

    protected fun grantBluetooth(onGranted: () -> Unit, onDenied: () -> Unit) =
        REQUIRED_PERMISSIONS.takeWhile {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED
        }.let {
            when {
                it.isEmpty() -> onGranted()
                else -> {
                    bluetoothGrantedObserver = onGranted
                    bluetoothDeniedObserver = onDenied
                    ActivityCompat.requestPermissions(
                        this,
                        it.toTypedArray(),
                        REQUEST_PERMISSION_RUNTIME
                    )
                }
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PERMISSION_RUNTIME ->
                grantResults.takeWhile {
                    it == PackageManager.PERMISSION_DENIED
                }.let {
                    when (it.isEmpty()) {
                        true -> bluetoothGrantedObserver()
                        false -> bluetoothDeniedObserver()
                    }
                }
            REQUEST_PERMISSION_ENABLE_BT ->
                when (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    true -> bluetoothGrantedObserver()
                    false -> bluetoothDeniedObserver()
                }
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_RUNTIME = 0
        private const val REQUEST_PERMISSION_ENABLE_BT = 1

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH
        )
    }
}
