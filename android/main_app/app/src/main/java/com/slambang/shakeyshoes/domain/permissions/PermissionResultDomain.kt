package com.slambang.shakeyshoes.domain.permissions

sealed class PermissionResultDomain {

    object Granted : PermissionResultDomain()

    data class Denied(
        val deniedPermissions: List<String>
    ) : PermissionResultDomain()
}
