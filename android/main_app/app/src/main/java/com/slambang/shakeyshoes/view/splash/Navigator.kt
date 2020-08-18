package com.slambang.shakeyshoes.view.splash

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.navigation.fragment.findNavController
import com.slambang.shakeyshoes.R
import javax.inject.Inject

interface SplashNavigator {

    fun navigateToAppPermissions()

    fun navigateToRcbView()
}

class SplashNavigatorImpl @Inject constructor(
    private val fragment: SplashViewFragment
) : SplashNavigator {

    override fun navigateToAppPermissions() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + fragment.context?.packageName)
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            fragment.startActivity(it)
        }
    }

    override fun navigateToRcbView() =
        fragment.findNavController().navigate(R.id.action_splash_fragment_to_rcb_fragment)
}