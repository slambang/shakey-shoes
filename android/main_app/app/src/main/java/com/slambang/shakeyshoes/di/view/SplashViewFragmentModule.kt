package com.slambang.shakeyshoes.di.view

import androidx.lifecycle.MutableLiveData
import com.slambang.shakeyshoes.domain.permissions.RuntimePermissionManager
import com.slambang.shakeyshoes.domain.permissions.RuntimePermissionManagerImpl
import com.slambang.shakeyshoes.view.splash.*
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [SplashViewFragmentModule.Bindings::class])
class SplashViewFragmentModule {

    @Provides
    fun provideSplashViewModelFactory(viewModel: SplashViewModel): ViewModelProviderFactory<SplashViewModel> =
        ViewModelProviderFactory(viewModel)

    @Provides
    fun provideRuntimePermissionsManager(fragment: SplashViewFragment): RuntimePermissionManager =
        RuntimePermissionManagerImpl(fragment)

    @Provides
    fun provideStatesLiveData(): MutableLiveData<SplashViewState> = MutableLiveData()

    @Provides
    fun provideSnackbarLiveData(): MutableLiveData<String> = MutableLiveData()

    @Module
    interface Bindings {

        @Binds
        fun provideNavigator(impl: SplashNavigatorImpl): SplashNavigator
    }
}
