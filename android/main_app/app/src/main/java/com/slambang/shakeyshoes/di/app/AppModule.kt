package com.slambang.shakeyshoes.di.app

import android.Manifest
import android.app.Application
import android.content.Context
import com.slambang.bluetooth_connection.BluetoothProvider
import com.slambang.bluetooth_connection.BluetoothProviderImpl
import com.slambang.shakeyshoes.di.scope.ApplicationContext
import com.slambang.shakeyshoes.entity.BluetoothDeviceRepository
import com.slambang.shakeyshoes.entity.BluetoothDeviceRepositoryImpl
import com.slambang.shakeyshoes.util.SchedulerProvider
import com.slambang.shakeyshoes.util.SchedulerProviderImpl
import com.slambang.shakeyshoes.util.StringProvider
import com.slambang.shakeyshoes.util.StringProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [AppModule.Bindings::class])
class AppModule {

    @Provides
    fun provideDisposable(): CompositeDisposable = CompositeDisposable()

    @Provides
    @Singleton
    fun provideBluetoothProvider(@ApplicationContext context: Context): BluetoothProvider =
        BluetoothProviderImpl.newInstance(context)

    @Provides
    @Singleton
    @Named(REQUIRED_RUNTIME_PERMISSIONS)
    fun provideRequiredRuntimePermissions(): List<String> =
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    @Provides
    fun provideSchedulerProvider(): SchedulerProvider = SchedulerProviderImpl()

    @Provides
    fun provideStringProvider(@ApplicationContext context: Context): StringProvider =
        StringProviderImpl(context)

    @Provides
    fun provideDeviceRepo(): BluetoothDeviceRepository = BluetoothDeviceRepositoryImpl()

    @Module
    interface Bindings {

        @Binds
        @Singleton
        @ApplicationContext
        fun bindContext(impl: Application): Context
    }
}
