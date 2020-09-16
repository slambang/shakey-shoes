package com.slambang.shakeyshoes.view

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.setAppCompatToolbar(toolbarId: Int) =
    (requireActivity() as AppCompatActivity).let {
        it.setSupportActionBar(
            it.findViewById(toolbarId)
        )
    }
