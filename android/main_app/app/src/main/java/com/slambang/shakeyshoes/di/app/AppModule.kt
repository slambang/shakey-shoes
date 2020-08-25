package com.slambang.shakeyshoes.di.app

import android.app.Application
import android.content.Context
import com.slambang.bluetooth_connection.BluetoothProvider
import com.slambang.bluetooth_connection.BluetoothProviderFactory
import com.slambang.shakeyshoes.di.scope.ApplicationContext
import com.slambang.shakeyshoes.entity.BluetoothDeviceRepository
import com.slambang.shakeyshoes.entity.BluetoothDeviceRepositoryImpl
import com.slambang.shakeyshoes.util.SchedulerProvider
import com.slambang.shakeyshoes.util.SchedulerProviderImpl
import com.slambang.shakeyshoes.util.StringProvider
import com.slambang.shakeyshoes.util.StringsProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AppModule.Bindings::class])
class AppModule {

    @Provides
    @Singleton
    fun provideBluetoothProvider(@ApplicationContext context: Context) : BluetoothProvider =
        BluetoothProviderFactory.newInstance(context)

    @Module
    interface Bindings {

        @Binds
        @Singleton
        @ApplicationContext
        fun bindContext(impl: Application): Context

        @Binds
        @Singleton
        fun bindSchedulers(impl: SchedulerProviderImpl): SchedulerProvider

        @Binds
        @Singleton
        fun bindStringResources(impl: StringsProviderImpl): StringProvider

        @Binds
        @Singleton
        fun bindDeviceRepo(impl: BluetoothDeviceRepositoryImpl): BluetoothDeviceRepository
    }
}
