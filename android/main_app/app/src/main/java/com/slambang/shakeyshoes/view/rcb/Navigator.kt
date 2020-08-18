package com.slambang.shakeyshoes.view.rcb

import android.content.Intent
import android.net.Uri
import javax.inject.Inject

interface RcbNavigator {

    fun navigateToUrl(url: String)
}

class RcbNavigatorImpl @Inject constructor(
    private val fragment: RcbViewFragment
) : RcbNavigator {

    override fun navigateToUrl(url: String) =
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .let { fragment.startActivity(it) }
}
