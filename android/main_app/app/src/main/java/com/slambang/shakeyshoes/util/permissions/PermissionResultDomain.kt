package com.slambang.shakeyshoes.util.permissions

sealed class PermissionResultDomain {

    object PermissionGranted : PermissionResultDomain()

    data class PermissionDenied(
        val deniedPermissions: List<String>
    ) : PermissionResultDomain()
}
