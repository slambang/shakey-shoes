package com.slambang.shakeyshoes.view.splash

import androidx.lifecycle.LiveData

interface SplashViewModel {

    val viewState: LiveData<SplashViewState>
    val snackBarState: LiveData<String>

    fun onStart()

    fun onPause()

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: List<String>,
        grantResults: List<Int>
    )

    fun onPermissionButtonClicked()
}
