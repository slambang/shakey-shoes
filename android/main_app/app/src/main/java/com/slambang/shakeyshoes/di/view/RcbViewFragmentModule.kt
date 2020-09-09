package com.slambang.shakeyshoes.di.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import com.slambang.rcb_service.RcbService
import com.slambang.shakeyshoes.audio.DataSource
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceOrchestrator
import com.slambang.shakeyshoes.domain.RcbServiceOrchestratorImpl
import com.slambang.shakeyshoes.domain.use_cases.DeviceRepositoryUseCase
import com.slambang.shakeyshoes.domain.use_cases.DeviceRepositoryUseCaseImpl
import com.slambang.shakeyshoes.domain.use_cases.RcbOrchestratorUseCase
import com.slambang.shakeyshoes.domain.use_cases.RcbOrchestratorUseCaseImpl
import com.slambang.shakeyshoes.view.base.SingleLiveEvent
import com.slambang.shakeyshoes.view.rcb.*
import com.slambang.shakeyshoes.view.rcb.mappers.BluetoothMessageMapper
import com.slambang.shakeyshoes.view.rcb.mappers.BluetoothMessageMapperImpl
import com.slambang.shakeyshoes.view.rcb.mappers.ErrorMapper
import com.slambang.shakeyshoes.view.rcb.mappers.ErrorMapperImpl
import com.slambang.shakeyshoes.view.rcb.rcb_item_view.BufferItemViewListener
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [RcbViewFragmentModule.Bindings::class])
class RcbViewFragmentModule {

    @Provides
    fun provideRcbViewModelFactory(viewModel: RcbViewModelImpl): ViewModelProviderFactory<RcbViewModelImpl> =
        ViewModelProviderFactory(viewModel)

    @Provides
    fun provideShowDeviceListLiveData(): SingleLiveEvent<List<Pair<Int, String>>> =
        SingleLiveEvent()

    @Provides
    fun provideBufferItemPageLiveData(): SingleLiveEvent<Pair<Int, Int>> = SingleLiveEvent()

    @Provides
    fun provideItemModelsLiveData(): MutableLiveData<Pair<RcbItemModel, Int>> = MutableLiveData()

    @Provides
    fun provideBluetoothStatusLiveData(): MutableLiveData<String> = MutableLiveData()

    @Provides
    fun provideRemoveAllBuffersLiveData(): SingleLiveEvent<Unit> = SingleLiveEvent()

    @Provides
    fun provideConfirmDialogLiveData(): SingleLiveEvent<DialogModel> = SingleLiveEvent()

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

    @Provides
    fun provideItemClickListener(
        fragment: RcbViewFragment,
        factory: ViewModelProviderFactory<RcbViewModelImpl>
    ): BufferItemViewListener =
        ViewModelProviders.of(fragment, factory).get(RcbViewModelImpl::class.java)

    @Module
    interface Bindings {

        @Binds
        fun bindErrorMapper(impl: ErrorMapperImpl): ErrorMapper

        @Binds
        fun bindRcbViewNavigator(impl: RcbViewNavigatorImpl): RcbViewNavigator

        @Binds
        fun bindBluetoothMessageMapper(impl: BluetoothMessageMapperImpl): BluetoothMessageMapper

        @Binds
        fun bindDeviceRepositoryUseCase(impl: DeviceRepositoryUseCaseImpl): DeviceRepositoryUseCase

        @Binds
        fun bindRcbServiceOrchestrator(impl: RcbServiceOrchestratorImpl): RcbServiceOrchestrator

        @Binds
        fun bindRcbOrchestratorUseCase(impl: RcbOrchestratorUseCaseImpl): RcbOrchestratorUseCase
    }
}
