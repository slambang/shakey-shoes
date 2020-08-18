package com.slambang.shakeyshoes.di.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class ViewModelProviderFactory<VMType : ViewModel>(private val mViewModel: VMType) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(mViewModel.javaClass)) {
            return mViewModel as T
        }

        throw IllegalArgumentException("Unexpected ViewModel class ${mViewModel.javaClass.name}")
    }
}
