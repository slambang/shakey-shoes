package com.slambang.shakeyshoes.view.splash

data class SplashViewState(
    val message: String,
    val showPermissionButton: Boolean,
    val permissionButtonText: String?
) {
    companion object {
        val DEFAULT = SplashViewState(
            "",
            false,
            null
        )
    }
}
