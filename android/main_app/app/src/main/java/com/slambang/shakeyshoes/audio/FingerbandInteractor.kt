package com.slambang.shakeyshoes.audio

import android.content.Intent
import android.util.Log
import java.io.File

interface FingerbandInteractor {
    fun start(intent: Intent)
}

class FingerbandInteractorImpl(
    private val jniBridge: JniBridge,
    private val mapper: IntentConfigMapper
) : FingerbandInteractor {

    override fun start(intent: Intent) {
        mapper.map(intent)?.let {
            onFingerbandEvent(it)
        }
    }

    private fun onFingerbandEvent(config: Config) {

        val result = jniBridge.runFingerband(config)

        val file = when (result) {
            0 -> "/success"
            else -> "/error"
        }

        writeResultFile(config, result, file)
    }

    private fun writeResultFile(config: Config, result: Int, file: String) =
        File(config.outputDir + file)
            .printWriter()
            .use {
                it.println("Result: $result")
                it.println(config)
            }
            .also { Log.d(javaClass.simpleName, "Result: $result") }
}
