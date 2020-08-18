package com.slambang.shakeyshoes

import android.app.Application
import com.slambang.shakeyshoes.di.app.AppComponent
import com.slambang.shakeyshoes.di.app.DaggerAppComponent
import dagger.android.HasAndroidInjector

class App : Application(), HasAndroidInjector {

    private lateinit var appComponent: AppComponent

    override fun androidInjector() = appComponent.androidInjector

    override fun onCreate() {
        super.onCreate()
        appComponent = createAppComponent()
    }

    private fun createAppComponent() =
        DaggerAppComponent.builder()
            .application(this)
            .build()
}
