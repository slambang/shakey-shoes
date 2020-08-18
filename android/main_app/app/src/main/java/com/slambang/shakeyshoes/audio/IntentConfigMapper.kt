package com.slambang.shakeyshoes.audio

import android.content.Intent
import java.lang.Exception

interface IntentConfigMapper {
    fun map(intent: Intent): Config?
}

class IntentConfigMapperImpl : IntentConfigMapper {

    override fun map(intent: Intent): Config? {

        if (!isConfigIntent(intent)) {
            return null
        }

        val bufferSize = intent.getIntExtra(BUFFER_SIZE, -1)
        val filterCenterHz = intent.getIntExtra(FILTER_CENTER_HZ, -1)
        val filterWidthHz = intent.getIntExtra(FILTER_WIDTH_HZ, -1)
        val threshold = intent.getFloatExtra(THRESHOLD, -1f)
        val windowType = intent.getIntExtra(WINDOW_TYPE, -1)
        val featureName = intent.getIntExtra(FEATURE_NAME, -1)
        val inputFile = intent.getStringExtra(INPUT_FILE)
        val outputDir = intent.getStringExtra(OUTPUT_DIR)

        if (bufferSize == -1 || filterCenterHz == -1 || filterWidthHz == -1 || threshold == -1f ||
            windowType == -1 || featureName == -1 || inputFile == null || outputDir == null
        ) {
            throw IntentConfigMapperException("One or more config fields were not found.")
        }

        val windowEnum = Window.values()[windowType]
        val featureNameEnum = SignalFeature.values()[featureName]

        return Config(
            filterCenterHz,
            filterWidthHz,
            bufferSize,
            threshold,
            windowEnum,
            featureNameEnum,
            inputFile,
            outputDir
        )
    }

    private fun isConfigIntent(intent: Intent) = intent.data?.toString() == FINGERBAND_ALPHA_SCHEMA

    class IntentConfigMapperException(val reason: String) : Exception(reason)

    companion object {
        private const val FINGERBAND_ALPHA_SCHEMA = "fingerband://alpha"

        private const val BUFFER_SIZE = "buffer_size"
        private const val FILTER_CENTER_HZ = "filter_center_hz"
        private const val FILTER_WIDTH_HZ = "filter_width_hz"
        private const val THRESHOLD = "threshold"
        private const val WINDOW_TYPE = "window_type"
        private const val FEATURE_NAME = "feature_name"
        private const val INPUT_FILE = "input_file"
        private const val OUTPUT_DIR = "output_dir"
    }
}
