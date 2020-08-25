package com.slambang.shakeyshoes.domain.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import javax.inject.Inject

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

class RuntimePermissionManagerImpl @Inject constructor(
    private val fragment: Fragment
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
        REQUIRED_RUNTIME_PERMISSIONS.none {
            ContextCompat.checkSelfPermission(
                fragment.requireActivity(),
                it
            ) != PackageManager.PERMISSION_GRANTED
        }

    override fun requestPermissions() =
        REQUIRED_RUNTIME_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(
                fragment.requireActivity(),
                it
            ) != PackageManager.PERMISSION_GRANTED
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
        for (i in grantResults.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permissions[i])
            }
        }

        return when (deniedPermissions.isEmpty()) {
            true -> PermissionResultDomain.PermissionGranted
            false -> PermissionResultDomain.PermissionDenied(deniedPermissions)
        }
    }

    companion object {
        private const val PERMISSION_MANAGER_REQUEST_CODE = 0

        // These should be injected
        private val REQUIRED_RUNTIME_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}
