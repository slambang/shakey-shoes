package com.slambang.shakeyshoes.di.app

import com.slambang.rcb_service.RcbServiceErrorMapper
import com.slambang.rcb_service.RcbStateMapper
import dagger.Module
import dagger.Provides

@Module
class RcbServiceModule {

    @Provides
    fun provideRcbStateMapper(): RcbStateMapper = RcbStateMapper()

    @Provides
    fun provideRcbServiceErrorMapper(): RcbServiceErrorMapper = RcbServiceErrorMapper()
}
