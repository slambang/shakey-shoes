package com.betty7.fingerband.alpha.bluetooth.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceInteractorImpl
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceOrchestrator
import com.betty7.fingerband.alpha.bluetooth.entity.BluetoothDeviceEntityMapper
import com.betty7.fingerband.alpha.bluetooth.entity.DeviceRepository
import com.betty7.fingerband.alpha.bluetooth.entity.DeviceRepositoryImpl
import com.betty7.fingerband.alpha.bluetooth.files.*
import com.betty7.rcb.*

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
    val interactor = provideCircularBufferInteractor(context)

    return RcbDemoActivityViewModelImpl(domainMapper, interactor)
}

private fun provideResources(context: Context): ViewResources = ViewResourcesImpl(context)

private fun provideSettableDataSource(): BeatMap = SettableBeatMap(INITIAL_VIBRATE_VALUE)

private fun provideDebugFileDataSource(): BeatMap {
    val fileReader = DebugFileReader()
    val valueMapper = DebugFileValueMapper()
    return FileBeatMap("/sdcard/fingerband/1/average_magnitude", fileReader, valueMapper).also { it.init(254, 0) }
}

private fun provideBluetoothConnection(context: Context): BluetoothConnection =
    BluetoothDevice.newInstance(context)

private fun provideMockBluetoothConnection(): BluetoothConnection =
    MockBluetoothDevice(1234)

private fun provideCircularBufferService(context: Context): CircularBufferService {
    val bluetoothConnection = provideBluetoothConnection(context)
    return CircularBufferServiceImpl(bluetoothConnection)
}

private fun provideDeviceEntityMapper() = BluetoothDeviceEntityMapper()

private fun provideDeviceRepository(): DeviceRepository = DeviceRepositoryImpl()

private fun provideCircularBufferInteractor(context: Context): RcbServiceOrchestrator {

    val entityMapper = provideDeviceEntityMapper()
    val deviceRepo = provideDeviceRepository()

    return RcbServiceInteractorImpl(
        deviceRepo,
        entityMapper,
        ::provideSettableDataSource
//        ::provideDebugFileDataSource
    ) {
        provideCircularBufferService(context)
    }
}

private fun provideDeviceDomainMapper(resources: ViewResources) = DomainMapper(resources)
