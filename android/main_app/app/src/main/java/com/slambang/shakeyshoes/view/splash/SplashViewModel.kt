package com.slambang.shakeyshoes.view.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slambang.shakeyshoes.util.SchedulerProvider
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val navigator: SplashNavigator,
    private val permissionUseCase: PermissionUseCase,
    private val schedulers: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val _viewState: MutableLiveData<SplashViewState>,
    private val _snackbarState: MutableLiveData<String>
) : ViewModel() {

    val viewState: LiveData<SplashViewState>
        get() = _viewState

    val snackbarState: LiveData<String>
        get() = _snackbarState

    fun onStart() {
        _viewState.postValue(SplashViewState.DEFAULT)

        permissionUseCase.checkPermissions()
            .delaySubscription(SPLASH_DELAY_MS, TimeUnit.MILLISECONDS)
            .genericSubscribe()
    }

    fun onPause() = onCleared()

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: List<String>,
        grantResults: List<Int>
    ) {
        permissionUseCase.checkPermissionResult(requestCode, permissions, grantResults)
            .genericSubscribe()
    }

    fun onPermissionButtonClicked() {
        permissionUseCase.reRequestPermission()
            .genericSubscribe()
    }

    private fun onSuccess(success: Boolean) {
        if (success) {
            navigator.navigateToRcbView()
        }
    }

    private fun onError(error: Throwable) {
        when (error) {
            is PermissionDeniedException -> _viewState.postValue(error.state)
            is RequirePermissionSettingsException -> navigator.navigateToAppPermissions()
            is RequestingPermissionException -> {
                /* no-op, the system is requesting permissions */
            }
        }
    }

    override fun onCleared() = disposables.clear()

    private fun Single<Boolean>.genericSubscribe() =
        subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribe(::onSuccess, ::onError)
            .also { disposables.add(it) }

    companion object {
        private const val SPLASH_DELAY_MS = 1500L
    }
}
