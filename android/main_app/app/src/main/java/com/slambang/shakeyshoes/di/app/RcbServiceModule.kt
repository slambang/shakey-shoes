package com.slambang.shakeyshoes.di.app

import com.slambang.rcb_service.RcbServiceErrorMapper
import com.slambang.rcb_service.RcbServiceErrorMapperImpl
import com.slambang.rcb_service.RcbStateMapper
import com.slambang.rcb_service.RcbStateMapperImpl
import dagger.Module
import dagger.Provides

@Module
class RcbServiceModule {

    @Provides
    fun provideRcbStateMapper(): RcbStateMapper = RcbStateMapperImpl()

    @Provides
    fun provideRcbServiceErrorMapper(): RcbServiceErrorMapper = RcbServiceErrorMapperImpl()
}
