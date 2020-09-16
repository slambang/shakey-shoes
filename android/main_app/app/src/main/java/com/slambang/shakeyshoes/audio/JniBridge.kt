package com.slambang.shakeyshoes.audio

enum class SignalFeature {
    Pitch,
    Note,
    RootMeanSquare,
    PeakEnergy,
    SpectralCrest,
    ZeroCrossingRate,
    SpecialCentroid,
    SpectralFlatness,
    SpectralDifference,
    SpectralDifferenceComplex,
    SpectralDifferenceHalfway,
    HighFrequencyCOntent
}

enum class Window {
    Rectangular,
    Hanning,
    Hamming,
    Blackman,
    Tukey
}

data class Config(
    val filterCenterHz: Int,
    val filterWidthHz: Int,
    val bufferSize: Int,
    val threshold: Float,
    val windowType: Window,
    val featureName: SignalFeature,
    val inputFile: String,
    val outputDir: String
)

interface JniBridge {
    fun runFingerband(config: Config): Int
}

class JniBridgeImpl: JniBridge {

    override fun runFingerband(config: Config) =
        runFingerbandNative(
            config.filterCenterHz,
            config.filterWidthHz,
            config.bufferSize,
            config.threshold,
            config.windowType.ordinal,
            config.featureName.ordinal,
            config.inputFile,
            config.outputDir
        )

    private external fun runFingerbandNative(
        filterCenter: Int,
        filterWidth: Int,
        bufferSize: Int,
        threshold: Float,
        windowType: Int,
        featureName: Int,
        inputFile: String,
        outputDir: String
    ): Int

    companion object {
        init {
            System.loadLibrary("fingerband-test")
        }
    }
}
