package com.betty7.fingerband.alpha.bluetooth.view

import android.content.Context

interface ViewResources {
    fun getString(resId: Int, vararg args: Any?): String
}

class ViewResourcesImpl(private val context: Context) : ViewResources {
    override fun getString(resId: Int, vararg args: Any?) = context.getString(resId, *args)
}
