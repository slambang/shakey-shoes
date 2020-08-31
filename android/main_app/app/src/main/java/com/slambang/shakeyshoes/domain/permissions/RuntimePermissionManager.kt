package com.slambang.shakeyshoes.domain.permissions

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

interface RuntimePermissionManager {

    val canReRequestPermissions: Boolean
    val isRequiredPermissionGranted: Boolean

    fun requestPermissions()

    fun getPermissionResult(
        requestCode: Int,
        permissions: List<String>,
        grantResults: List<Int>
    ): PermissionResultDomain
}

// This needs to be decoupled from Android classes for easy unit testing.
class RuntimePermissionManagerImpl constructor(
    private val fragment: Fragment,
    private val requiredPermissions: List<String>
) : RuntimePermissionManager {

    override val canReRequestPermissions: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.R

    override val isRequiredPermissionGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkRuntimePermissionsGranted()
        } else {
            true
        }

    private fun checkRuntimePermissionsGranted(): Boolean =
        requiredPermissions.none {
            ContextCompat.checkSelfPermission(
                fragment.requireActivity(),
                it
            ) == PackageManager.PERMISSION_DENIED
        }

    override fun requestPermissions() =
        requiredPermissions.filter {
            ContextCompat.checkSelfPermission(
                fragment.requireActivity(),
                it
            ) == PackageManager.PERMISSION_DENIED
        }.let {
            fragment.requestPermissions(
                it.toTypedArray(),
                PERMISSION_MANAGER_REQUEST_CODE
            )
        }

    @Throws(IllegalArgumentException::class)
    override fun getPermissionResult(
        requestCode: Int,
        permissions: List<String>,
        grantResults: List<Int>
    ): PermissionResultDomain {
        if (requestCode != PERMISSION_MANAGER_REQUEST_CODE) {
            throw IllegalArgumentException("Unexpected request code $requestCode")
        }

        val deniedPermissions = mutableListOf<String>()
        grantResults.forEachIndexed { index, grantResult ->
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permissions[index])
            }
        }

        return when (deniedPermissions.isEmpty()) {
            true -> PermissionResultDomain.PermissionGranted
            false -> PermissionResultDomain.PermissionDenied(deniedPermissions)
        }
    }

    companion object {
        private const val PERMISSION_MANAGER_REQUEST_CODE = 0
    }
}
