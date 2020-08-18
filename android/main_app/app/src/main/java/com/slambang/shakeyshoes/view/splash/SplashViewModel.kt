package com.slambang.shakeyshoes.view.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slambang.rcb.bluetooth.BluetoothProvider
import com.slambang.shakeyshoes.util.SchedulerProvider
import com.slambang.shakeyshoes.util.permissions.PermissionResultDomain.PermissionGranted
import com.slambang.shakeyshoes.util.permissions.PermissionResultDomain.PermissionDenied
import com.slambang.shakeyshoes.util.permissions.RuntimePermissionManager
import com.slambang.shakeyshoes.view.splash.mappers.BluetoothMessageMapper
import com.slambang.shakeyshoes.view.splash.mappers.PermissionDeniedMessageMapper
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// TODO: Improve threading here
class SplashViewModel @Inject constructor(
    private val navigator: SplashNavigator,
    private val bluetoothProvider: BluetoothProvider,
    private val permissionManager: RuntimePermissionManager,
    private val bluetoothMessageMapper: BluetoothMessageMapper,
    private val permissionMessageMapper: PermissionDeniedMessageMapper,
    private val schedulers: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val _viewState: MutableLiveData<SplashViewState>
) : ViewModel() {

    val viewState: LiveData<SplashViewState>
        get() = _viewState

    fun onStart() {
        checkDeviceReadiness()
            .delaySubscription(SPLASH_DELAY_MS, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulers.io)
            .subscribe()
            .also { disposables.add(it) }
    }

    fun onPause() = onCleared()

    private fun checkDeviceReadiness() =
        Single.fromCallable {
            if (!permissionManager.isRequiredPermissionGranted) {
                permissionManager.requestPermissions()
            } else {
                checkBluetoothState()
            }
        }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: List<String>,
        grantResults: List<Int>
    ) {
        permissionManager.getPermissionResult(requestCode, permissions, grantResults)
            .let {
                when (it) {
                    is PermissionGranted -> checkBluetoothState()
                    is PermissionDenied -> handlePermissionDenied(it)
                }
            }
    }

    fun onPermissionButtonClicked() =
        if (permissionManager.canReRequestPermissions) {
            permissionManager.requestPermissions()
        } else {
            navigator.navigateToAppPermissions()
        }

    private fun checkBluetoothState() {
        if (!bluetoothProvider.isBluetoothReady) {
            bluetoothMessageMapper.map(
                bluetoothProvider.isBluetoothAvailable,
                bluetoothProvider.isBluetoothEnabled
            ).also { emitState(it) }
        } else {
            navigator.navigateToRcbView()
        }
    }

    private fun handlePermissionDenied(result: PermissionDenied) =
        permissionMessageMapper.map(result, permissionManager.canReRequestPermissions)
            .also { emitState(it) }

    private fun emitState(state: SplashViewState) {
        _viewState.value = state
    }

    override fun onCleared() = disposables.clear()

    companion object {
        private const val SPLASH_DELAY_MS = 1500L
    }
}
