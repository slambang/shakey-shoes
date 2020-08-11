package com.betty7.fingerband.alpha.bluetooth.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceRepositoryInteractor
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceInteractorImpl
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceOrchestrator
import com.betty7.fingerband.alpha.bluetooth.entity.BluetoothDeviceEntityMapper
import com.betty7.fingerband.alpha.bluetooth.entity.DeviceRepository
import com.betty7.fingerband.alpha.bluetooth.entity.DeviceRepositoryImpl
import com.betty7.fingerband.alpha.bluetooth.files.RcbDataSource
import com.betty7.fingerband.alpha.bluetooth.files.SettableRcbDataSource
import com.betty7.rcb.BluetoothConnection
import com.betty7.rcb.BluetoothDevice
import com.betty7.rcb.CircularBufferService
import com.betty7.rcb.CircularBufferServiceImpl

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
    val rcbServiceOrchestrator = provideRcbServiceOrchestrator(context)
    val deviceRepoInteractor = provideDeviceRepoInteractor()

    return RcbDemoActivityViewModelImpl(domainMapper, rcbServiceOrchestrator, deviceRepoInteractor)
}

private fun provideDeviceRepoInteractor(): DeviceRepositoryInteractor {
    val entityMapper = provideDeviceEntityMapper()
    val deviceRepo = provideDeviceRepository()
    return DeviceRepositoryInteractor(deviceRepo, entityMapper)
}

private fun provideResources(context: Context): ViewResources = ViewResourcesImpl(context)

private fun provideSettableDataSource(): RcbDataSource =
    SettableRcbDataSource(INITIAL_VIBRATE_VALUE)

private fun provideBluetoothConnection(context: Context): BluetoothConnection =
    BluetoothDevice.newInstance(context)

private fun provideCircularBufferService(context: Context): CircularBufferService {
    val bluetoothConnection = provideBluetoothConnection(context)
    return CircularBufferServiceImpl(bluetoothConnection)
}

private fun provideDeviceEntityMapper() = BluetoothDeviceEntityMapper()

private fun provideDeviceRepository(): DeviceRepository = DeviceRepositoryImpl()

private fun provideRcbServiceOrchestrator(context: Context): RcbServiceOrchestrator {

    return RcbServiceInteractorImpl(
        ::provideSettableDataSource,
        { provideCircularBufferService(context) }
    )
}

private fun provideDeviceDomainMapper(resources: ViewResources) = DomainMapper(resources)
