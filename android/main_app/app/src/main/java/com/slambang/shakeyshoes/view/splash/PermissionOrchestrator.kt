package com.slambang.shakeyshoes.view.splash

import com.slambang.shakeyshoes.domain.permissions.PermissionResultDomain
import com.slambang.shakeyshoes.domain.permissions.RuntimePermissionManager
import io.reactivex.Single
import javax.inject.Inject

class PermissionDeniedException(val state: SplashViewState) : Exception()

class RequirePermissionSettingsException : Exception()

class RequestingPermissionException : Exception()

class PermissionUseCase @Inject constructor(
    private val permissionManager: RuntimePermissionManager,
    private val permissionMessageMapper: PermissionDeniedMessageMapper
) {

    fun checkPermissions() =
        Single.fromCallable {
            if (!permissionManager.isRequiredPermissionGranted) {
                permissionManager.requestPermissions()
                throw RequestingPermissionException()
            } else {
                true
            }
        }

    fun checkPermissionResult(
        requestCode: Int,
        permissions: List<String>,
        grantResults: List<Int>
    ) = Single.fromCallable {
        when (val result =
            permissionManager.getPermissionResult(requestCode, permissions, grantResults)) {
            is PermissionResultDomain.Granted -> true
            is PermissionResultDomain.Denied -> throw getPermissionException(result)
        }
    }

    fun reRequestPermission() =
        Single.fromCallable {
            if (permissionManager.canReRequestPermissions) {
                permissionManager.requestPermissions()
                true
            } else {
                throw RequirePermissionSettingsException()
            }
        }

    private fun getPermissionException(result: PermissionResultDomain.Denied) =
        PermissionDeniedException(
            permissionMessageMapper.map(result, permissionManager.canReRequestPermissions)
        )
}
