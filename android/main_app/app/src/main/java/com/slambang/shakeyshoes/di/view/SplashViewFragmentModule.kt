package com.slambang.shakeyshoes.di.view

import androidx.lifecycle.MutableLiveData
import com.slambang.shakeyshoes.util.permissions.RuntimePermissionManager
import com.slambang.shakeyshoes.util.permissions.RuntimePermissionManagerImpl
import com.slambang.shakeyshoes.view.splash.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

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

    @Provides
    fun provideDisposable(): CompositeDisposable = CompositeDisposable()

    @Module
    interface Bindings {

        @Binds
        fun provideNavigator(navigator: SplashNavigatorImpl): SplashNavigator
    }
}
