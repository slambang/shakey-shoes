package com.slambang.shakeyshoes.di.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import com.slambang.rcb_service.RcbService
import com.slambang.shakeyshoes.audio.DataSource
import com.slambang.shakeyshoes.di.factories.RcbDataFactory
import com.slambang.shakeyshoes.di.factories.RcbServiceFactory
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceOrchestrator
import com.slambang.shakeyshoes.domain.RcbServiceOrchestratorImpl
import com.slambang.shakeyshoes.domain.RcbServiceStatusMapper
import com.slambang.shakeyshoes.domain.use_cases.DeviceRepositoryUseCase
import com.slambang.shakeyshoes.domain.use_cases.DeviceRepositoryUseCaseImpl
import com.slambang.shakeyshoes.domain.use_cases.RcbOrchestratorUseCase
import com.slambang.shakeyshoes.domain.use_cases.RcbOrchestratorUseCaseImpl
import com.slambang.shakeyshoes.entity.BluetoothDeviceEntityMapper
import com.slambang.shakeyshoes.entity.BluetoothDeviceRepository
import com.slambang.shakeyshoes.util.StringProvider
import com.slambang.shakeyshoes.util.TimeProvider
import com.slambang.shakeyshoes.util.TimeProviderImpl
import com.slambang.shakeyshoes.view.base.SingleLiveEvent
import com.slambang.shakeyshoes.view.rcb.*
import com.slambang.shakeyshoes.view.rcb.mappers.*
import com.slambang.shakeyshoes.view.rcb.rcb_item_view.BufferItemViewListener
import dagger.Module
import dagger.Provides

@Module
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

    @Provides
    fun provideErrorMapper(strings: StringProvider): ErrorMapper = ErrorMapperImpl(strings)

    @Provides
    fun provideTimeProvider(): TimeProvider = TimeProviderImpl()

    @Provides
    fun provideRcbViewNavigator(
        fragment: RcbViewFragment,
        productUrlMapper: ProductUrlMapper
    ): RcbViewNavigator =
        RcbViewNavigatorImpl(fragment, productUrlMapper)

    @Provides
    fun provideBluetoothMessageMapper(strings: StringProvider): BluetoothMessageMapper =
        BluetoothMessageMapperImpl(strings)

    @Provides
    fun provideDeviceRepositoryUseCase(
        deviceRepo: BluetoothDeviceRepository,
        entityMapper: BluetoothDeviceEntityMapper,
        reservedDevices: MutableSet<Int>
    ): DeviceRepositoryUseCase =
        DeviceRepositoryUseCaseImpl(deviceRepo, entityMapper, reservedDevices)

    @Provides
    fun provideRcbServiceOrchestrator(
        rcbDataFactory: RcbDataFactory,
        rcbServiceFactory: RcbServiceFactory,
        dataSources: MutableMap<Int, DataSource>,
        rcbServices: MutableMap<Int, RcbService>,
        serviceStatusMapper: RcbServiceStatusMapper
    ): RcbServiceOrchestrator =
        RcbServiceOrchestratorImpl(
            rcbDataFactory,
            rcbServiceFactory,
            dataSources,
            rcbServices,
            serviceStatusMapper
        )

    @Provides
    fun provideRcbOrchestratorUseCase(
        rcbServiceOrchestrator: RcbServiceOrchestrator,
        deviceRepoUseCase: DeviceRepositoryUseCaseImpl,
        domainMap: MutableMap<Int, BluetoothDeviceDomain>
    ): RcbOrchestratorUseCase =
        RcbOrchestratorUseCaseImpl(rcbServiceOrchestrator, deviceRepoUseCase, domainMap)
}
