package com.slambang.shakeyshoes.util

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface SchedulerProvider {
    val main: Scheduler
    val io: Scheduler
    val newThread: Scheduler
}

class SchedulerProviderImpl @Inject constructor(): SchedulerProvider {
    override val main: Scheduler = AndroidSchedulers.mainThread()
    override val io: Scheduler = Schedulers.io()
    override val newThread: Scheduler = Schedulers.newThread()
}
