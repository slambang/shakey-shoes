package com.slambang.shakeyshoes.util

import android.content.Context
import androidx.annotation.StringRes
import com.slambang.shakeyshoes.di.scope.ApplicationContext
import javax.inject.Inject

interface StringProvider {
    fun getString(@StringRes resourceId: Int, vararg args: Any): String
}

class StringProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StringProvider {

    override fun getString(@StringRes resourceId: Int, vararg args: Any): String =
        context.getString(resourceId, *args)
}
