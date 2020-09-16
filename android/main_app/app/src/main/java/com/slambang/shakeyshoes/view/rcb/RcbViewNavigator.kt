package com.slambang.shakeyshoes.view.rcb

import android.content.Intent
import android.net.Uri
import com.slambang.shakeyshoes.view.rcb.mappers.ProductUrlMapper
import javax.inject.Inject

interface RcbViewNavigator {

    fun navigateToRepo()

    fun navigateToProduct(deviceId: Int)
}

class RcbViewNavigatorImpl @Inject constructor(
    private val fragment: RcbViewFragment,
    private val productUrlMapper: ProductUrlMapper
) : RcbViewNavigator {

    override fun navigateToRepo() = navigateToUrl(PROJECT_REPO_URL)

    override fun navigateToProduct(deviceId: Int) {
        navigateToUrl(
            url = productUrlMapper.map(deviceId)
        )
    }

    private fun navigateToUrl(url: String) =
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).let {
            fragment.startActivity(it)
        }

    companion object {
        const val PROJECT_REPO_URL = "https://github.com/slambang/shakey_shoes"
    }
}
