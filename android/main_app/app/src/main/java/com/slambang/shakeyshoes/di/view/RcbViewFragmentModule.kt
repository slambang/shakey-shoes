package com.slambang.shakeyshoes.di.view

import androidx.lifecycle.MutableLiveData
import com.slambang.rcb_service.RcbService
import com.slambang.shakeyshoes.audio.DataSource
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceOrchestrator
import com.slambang.shakeyshoes.domain.RcbServiceOrchestratorImpl
import com.slambang.shakeyshoes.domain.use_cases.DeviceRepositoryUseCase
import com.slambang.shakeyshoes.domain.use_cases.DeviceRepositoryUseCaseImpl
import com.slambang.shakeyshoes.view.base.SingleLiveEvent
import com.slambang.shakeyshoes.view.rcb.RcbItemModel
import com.slambang.shakeyshoes.view.rcb.RcbNavigator
import com.slambang.shakeyshoes.view.rcb.RcbNavigatorImpl
import com.slambang.shakeyshoes.view.rcb.RcbViewModel
import com.slambang.shakeyshoes.view.rcb.mappers.BluetoothMessageMapper
import com.slambang.shakeyshoes.view.rcb.mappers.BluetoothMessageMapperImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [RcbViewFragmentModule.Bindings::class])
class RcbViewFragmentModule {

    @Provides
    fun provideRcbViewModelFactory(viewModel: RcbViewModel): ViewModelProviderFactory<RcbViewModel> =
        ViewModelProviderFactory(viewModel)

    @Provides
    fun provideShowDeviceListLiveData(): SingleLiveEvent<List<Pair<Int, String>>> = SingleLiveEvent()

    @Provides
    fun provideBufferItemPageLiveData(): SingleLiveEvent<Pair<Int, Int>> = SingleLiveEvent()

    @Provides
    fun provideItemModelsLiveData(): MutableLiveData<List<RcbItemModel>> = MutableLiveData()

    @Provides
    fun provideBluetoothStatusLiveData(): MutableLiveData<String> = MutableLiveData()

    @Provides
    fun provideRemoveAllBuffersLiveData(): SingleLiveEvent<Unit> = SingleLiveEvent()

    @Provides
    fun provideRemoveAllMenuOptionEnabledLiveData(): SingleLiveEvent<Boolean> = SingleLiveEvent()

    @Provides
    fun provideItemDeletedLiveData(): SingleLiveEvent<Int> = SingleLiveEvent()

    @Provides
    fun provideErrorLiveData(): SingleLiveEvent<String> = SingleLiveEvent()

    @Provides
    fun provideItemOrderedList(): MutableList<RcbItemModel> = mutableListOf()

    @Provides
    fun provideReservedDevices(): MutableSet<Int> = mutableSetOf()

    @Provides
    fun provideDomainMap(): MutableMap<Int, BluetoothDeviceDomain> = mutableMapOf()

    @Provides
    fun provideRcbDataSources(): MutableMap<Int, DataSource> = mutableMapOf()

    @Provides
    fun provideRcbServices(): MutableMap<Int, RcbService> = mutableMapOf()

    @Module
    interface Bindings {

        @Binds
        fun bindBluetoothMessageMapper(impl: BluetoothMessageMapperImpl): BluetoothMessageMapper

        @Binds
        fun bindDeviceRepositoryUseCase(impl: DeviceRepositoryUseCaseImpl): DeviceRepositoryUseCase

        @Binds
        fun provideNavigator(impl: RcbNavigatorImpl): RcbNavigator

        @Binds
        fun provideRcbServiceOrchestrator(impl: RcbServiceOrchestratorImpl): RcbServiceOrchestrator
    }
}
