package com.betty7.fingerband.alpha.bluetooth.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.betty7.fingerband.alpha.bluetooth.data.audio.RcbDataSource
import com.betty7.fingerband.alpha.bluetooth.data.audio.SettableRcbDataSource
import com.betty7.fingerband.alpha.bluetooth.data.entity.BluetoothDeviceRepository
import com.betty7.fingerband.alpha.bluetooth.data.entity.BluetoothDeviceRepositoryImpl
import com.betty7.fingerband.alpha.bluetooth.domain.BluetoothDeviceEntityMapper
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceRepositoryInteractor
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceInteractorImpl
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceOrchestrator
import com.slambang.rcb.bluetooth.BluetoothConnection
import com.slambang.rcb.bluetooth.BluetoothDevice
import com.slambang.rcb.service.RcbService
import com.slambang.rcb.service.RcbServiceImpl

class RcbDemoViewModelFactory constructor(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        if (modelClass.isAssignableFrom(RcbDemoActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            provideViewModel(context) as T
        } else {
            throw IllegalArgumentException("ViewModel not found")
        }
}

// TODO Koin
private fun provideViewModel(context: Context): RcbDemoActivityViewModelImpl {

    val resources = provideResources(context)
    val domainMapper = provideDeviceDomainMapper(resources)
    val deviceRepoInteractor = provideDeviceRepoInteractor()
    val orchestratorInteractor = provideRcbOrchestratorInteractor(context, deviceRepoInteractor)

    return RcbDemoActivityViewModelImpl(domainMapper, deviceRepoInteractor, orchestratorInteractor)
}

private fun provideRcbOrchestratorInteractor(context: Context, repositoryInteractor: DeviceRepositoryInteractor): RcbOrchestratorInteractor {
    val rcbServiceOrchestrator = provideRcbServiceOrchestrator(context)
    return RcbOrchestratorInteractor(rcbServiceOrchestrator, repositoryInteractor)
}

private fun provideDeviceRepoInteractor(): DeviceRepositoryInteractor {
    val entityMapper = provideDeviceEntityMapper()
    val deviceRepo = provideDeviceRepository()
    return DeviceRepositoryInteractor(deviceRepo, entityMapper)
}

private fun provideResources(context: Context): ViewResources = ViewResourcesImpl(context)

private fun provideSettableDataSource(): RcbDataSource =
    SettableRcbDataSource(
        INITIAL_VIBRATE_VALUE
    )

private fun provideBluetoothConnection(context: Context): BluetoothConnection =
    BluetoothDevice.newInstance(context)

private fun provideCircularBufferService(context: Context): RcbService {
    val bluetoothConnection = provideBluetoothConnection(context)
    return RcbServiceImpl(bluetoothConnection)
}

private fun provideDeviceEntityMapper() =
    BluetoothDeviceEntityMapper()

private fun provideDeviceRepository(): BluetoothDeviceRepository = BluetoothDeviceRepositoryImpl()

private fun provideRcbServiceOrchestrator(context: Context): RcbServiceOrchestrator {

    return RcbServiceInteractorImpl(
        ::provideSettableDataSource,
        { provideCircularBufferService(context) }
    )
}

private fun provideDeviceDomainMapper(resources: ViewResources) = DomainMapper(resources)
