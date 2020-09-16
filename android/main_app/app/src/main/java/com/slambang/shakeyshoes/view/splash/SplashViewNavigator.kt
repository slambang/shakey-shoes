package com.slambang.shakeyshoes.view.splash

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.slambang.shakeyshoes.R
import javax.inject.Inject

interface SplashNavigator {

    fun navigateToRcbView()

    fun navigateToAppPermissions()
}

class SplashNavigatorImpl @Inject constructor(
    private val fragment: Fragment
) : SplashNavigator {

    override fun navigateToRcbView() =
        fragment.findNavController().navigate(R.id.action_splash_fragment_to_rcb_fragment)

    override fun navigateToAppPermissions() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${fragment.context?.packageName}")
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            fragment.startActivity(it)
        }
    }
}
