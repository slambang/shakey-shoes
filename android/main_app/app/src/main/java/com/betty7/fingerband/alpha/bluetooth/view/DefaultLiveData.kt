package com.betty7.fingerband.alpha.bluetooth.view

import androidx.lifecycle.MutableLiveData

class DefaultLiveData<T>(default: T) : MutableLiveData<T>(default) {

    override fun getValue() = super.getValue()!!

    fun repostValue() = postValue(value)
}
