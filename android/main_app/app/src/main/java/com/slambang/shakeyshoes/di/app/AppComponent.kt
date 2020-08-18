package com.slambang.shakeyshoes.di.app

import android.app.Application
import com.slambang.shakeyshoes.di.view.FragmentBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        FragmentBuilder::class
    ]
)
interface AppComponent {

    val androidInjector: DispatchingAndroidInjector<Any>

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}
