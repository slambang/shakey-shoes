package com.slambang.shakeyshoes.di.view

import androidx.lifecycle.MutableLiveData
import com.slambang.shakeyshoes.di.app.REQUIRED_RUNTIME_PERMISSIONS
import com.slambang.shakeyshoes.domain.permissions.RuntimePermissionManager
import com.slambang.shakeyshoes.domain.permissions.RuntimePermissionManagerImpl
import com.slambang.shakeyshoes.view.splash.*
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class SplashViewFragmentModule {

    @Provides
    fun provideSplashViewModelFactory(viewModelImpl: SplashViewModelImpl): ViewModelProviderFactory<SplashViewModelImpl> =
        ViewModelProviderFactory(viewModelImpl)

    @Provides
    fun provideRuntimePermissionsManager(
        fragment: SplashViewFragment,
        @Named(REQUIRED_RUNTIME_PERMISSIONS) requiredPermissions: List<String>
    ): RuntimePermissionManager =
        RuntimePermissionManagerImpl(fragment, requiredPermissions)

    @Provides
    fun provideStatesLiveData(): MutableLiveData<SplashViewState> = MutableLiveData()

    @Provides
    fun provideSnackBarLiveData(): MutableLiveData<String> = MutableLiveData()

    @Provides
    fun provideSplashNavigator(fragment: SplashViewFragment): SplashNavigator =
        SplashNavigatorImpl(fragment)
}
