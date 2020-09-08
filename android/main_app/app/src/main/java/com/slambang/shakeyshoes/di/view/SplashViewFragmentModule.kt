package com.slambang.shakeyshoes.di.view

import androidx.lifecycle.MutableLiveData
import com.slambang.shakeyshoes.di.app.REQUIRED_RUNTIME_PERMISSIONS
import com.slambang.shakeyshoes.domain.permissions.RuntimePermissionManager
import com.slambang.shakeyshoes.domain.permissions.RuntimePermissionManagerImpl
import com.slambang.shakeyshoes.view.splash.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module(includes = [SplashViewFragmentModule.Bindings::class])
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
    fun provideSnackbarLiveData(): MutableLiveData<String> = MutableLiveData()

    @Module
    interface Bindings {

        @Binds
        fun bindNavigator(impl: SplashNavigatorImpl): SplashNavigator
    }
}
