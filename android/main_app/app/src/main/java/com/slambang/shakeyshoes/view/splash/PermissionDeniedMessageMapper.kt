package com.slambang.shakeyshoes.view.splash

import android.Manifest.permission.*
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.domain.permissions.PermissionResultDomain
import com.slambang.shakeyshoes.util.StringProvider
import javax.inject.Inject

class PermissionDeniedMessageMapper @Inject constructor(
    private val strings: StringProvider
) {

    @Throws(IllegalArgumentException::class)
    fun map(
        domain: PermissionResultDomain.Denied,
        canRequestPermissions: Boolean
    ): SplashViewState {

        val mappedPermissionNames = domain.deniedPermissions.joinToString { mapPermissionName(it) }
        val message = mapMessage(mappedPermissionNames)
        val buttonText = mapButtonText(canRequestPermissions)

        return SplashViewState(
            message,
            true,
            buttonText
        )
    }

    private fun mapMessage(requiredPermissions: String) =
        strings.getString(R.string.permissions_denied_list, requiredPermissions)

    private fun mapButtonText(canRequestPermissions: Boolean) =
        if (canRequestPermissions) {
            strings.getString(R.string.permission_button_enable)
        } else {
            strings.getString(R.string.permission_button_visit_settings)
        }

    private fun mapPermissionName(permission: String) =
        when (permission) {
            ACCESS_FINE_LOCATION -> strings.getString(R.string.permission_fine_location)
            READ_EXTERNAL_STORAGE -> strings.getString(R.string.permission_read_external_storage)
            WRITE_EXTERNAL_STORAGE -> strings.getString(R.string.permission_write_external_storage)
            else -> throw IllegalArgumentException("Unexpected permission $permission")
        }
}
