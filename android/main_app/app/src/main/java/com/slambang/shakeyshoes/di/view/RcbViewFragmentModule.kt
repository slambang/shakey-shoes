package com.slambang.shakeyshoes.di.view

import androidx.lifecycle.MutableLiveData
import com.slambang.rcb.service.RcbService
import com.slambang.shakeyshoes.data.audio.RcbDataSource
import com.slambang.shakeyshoes.data.entity.BluetoothDeviceRepository
import com.slambang.shakeyshoes.data.entity.BluetoothDeviceRepositoryImpl
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceOrchestratorImpl
import com.slambang.shakeyshoes.domain.RcbServiceOrchestrator
import com.slambang.shakeyshoes.view.base.SingleLiveEvent
import com.slambang.shakeyshoes.view.rcb.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module(includes = [RcbViewFragmentModule.Bindings::class])
class RcbViewFragmentModule {

    @Provides
    fun provideRcbViewModelFactory(viewModel: RcbViewModel): ViewModelProviderFactory<RcbViewModel> =
        ViewModelProviderFactory(viewModel)

    @Provides
    fun provideDisposable(): CompositeDisposable = CompositeDisposable()

    @Provides
    fun provideShowDeviceListLiveData(): SingleLiveEvent<List<Pair<Int, String>>> = SingleLiveEvent()

    @Provides
    fun provideBufferItemPageLiveData(): SingleLiveEvent<Pair<Int, Int>> = SingleLiveEvent()

    @Provides
    fun provideItemModelsLiveData(): MutableLiveData<List<RcbItemModel>> = MutableLiveData()

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
    fun provideRcbDataSources(): MutableMap<Int, RcbDataSource> = mutableMapOf()

    @Provides
    fun provideRcbServices(): MutableMap<Int, RcbService> = mutableMapOf()

    @Module
    interface Bindings {

        @Binds
        fun bindViewModel(viewModel: RcbViewModelImpl): RcbViewModel

        @Binds
        fun bindDeviceRepo(repo: BluetoothDeviceRepositoryImpl): BluetoothDeviceRepository

        @Binds
        fun provideNavigator(navigator: RcbNavigatorImpl): RcbNavigator

        @Binds
        fun provideRcbServiceOrchestrator(orchestrator: RcbServiceOrchestratorImpl): RcbServiceOrchestrator
    }
}
