package com.slambang.shakeyshoes.di.view

import com.slambang.shakeyshoes.di.scope.FragmentViewScope
import com.slambang.shakeyshoes.view.rcb.RcbViewFragment
import com.slambang.shakeyshoes.view.splash.SplashViewFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface FragmentBuilder {

    @FragmentViewScope
    @ContributesAndroidInjector(modules = [SplashViewFragmentModule::class])
    fun contributeSplashFragment(): SplashViewFragment

    @FragmentViewScope
    @ContributesAndroidInjector(modules = [RcbViewFragmentModule::class])
    fun contributeRcbFragment(): RcbViewFragment
}
