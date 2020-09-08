package com.slambang.shakeyshoes.view.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slambang.shakeyshoes.util.SchedulerProvider
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashViewModelImpl @Inject constructor(
    private val navigator: SplashNavigator,
    private val schedulers: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val permissionUseCase: PermissionUseCase,
    // LiveData
    private val _snackBarState: MutableLiveData<String>,
    private val _viewState: MutableLiveData<SplashViewState>
) : SplashViewModel, ViewModel() {

    override val viewState: LiveData<SplashViewState>
        get() = _viewState

    override val snackBarState: LiveData<String>
        get() = _snackBarState

    override fun onStart() =
        observe(
            permissionUseCase.checkPermissions()
                .delaySubscription(SPLASH_DELAY_MS, TimeUnit.MILLISECONDS)
        ).also {
            _viewState.postValue(SplashViewState.DEFAULT)
        }

    override fun onPause() = onCleared()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: List<String>,
        grantResults: List<Int>
    ) = observe(
        permissionUseCase.checkPermissionResult(requestCode, permissions, grantResults)
    )

    override fun onPermissionButtonClicked() = observe(
        permissionUseCase.reRequestPermission()
    )

    override fun onCleared() = disposables.clear()

    private fun observe(source: Single<Boolean>) {
        source.subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribe(::onSuccess, ::onError)
            .also { disposables.add(it) }
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

    companion object {
        const val SPLASH_DELAY_MS = 1500L
    }
}
