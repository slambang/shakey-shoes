package com.slambang.shakeyshoes

import com.slambang.shakeyshoes.di.app.AppComponent
import com.slambang.shakeyshoes.di.app.DaggerAppComponent
import dagger.android.DaggerApplication

class App : DaggerApplication() {

    private val appComponent: AppComponent =
        DaggerAppComponent.builder()
            .application(this)
            .build()

    override fun applicationInjector() = appComponent
}
