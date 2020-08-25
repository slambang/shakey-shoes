package com.slambang.shakeyshoes.domain.permissions

sealed class PermissionResultDomain {

    object PermissionGranted : PermissionResultDomain()

    data class PermissionDenied(
        val deniedPermissions: List<String>
    ) : PermissionResultDomain()
}
