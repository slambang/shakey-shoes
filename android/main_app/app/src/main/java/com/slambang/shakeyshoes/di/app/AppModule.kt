package com.slambang.shakeyshoes.di.app

import android.app.Application
import android.content.Context
import com.slambang.rcb.bluetooth.BluetoothProvider
import com.slambang.rcb.bluetooth.BluetoothProviderFactory
import com.slambang.shakeyshoes.di.scope.ApplicationContext
import com.slambang.shakeyshoes.util.SchedulerProvider
import com.slambang.shakeyshoes.util.SchedulerProviderImpl
import com.slambang.shakeyshoes.util.StringProvider
import com.slambang.shakeyshoes.util.StringsProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import javax.inject.Singleton

@Module(includes = [AppModule.Bindings::class])
class AppModule {

    @Provides
    @Reusable
    fun provideBluetoothProvider(@ApplicationContext context: Context) : BluetoothProvider =
        BluetoothProviderFactory.newInstance(context)

    @Module
    interface Bindings {

        @Binds
        @Singleton
        @ApplicationContext
        fun bindContext(impl: Application): Context

        @Binds
        @Reusable
        fun bindSchedulers(impl: SchedulerProviderImpl): SchedulerProvider

        @Binds
        @Reusable
        fun bindStringResources(impl: StringsProviderImpl): StringProvider
    }
}
