package com.slambang.shakeyshoes.oboe

import android.content.Context
import com.slambang.shakeyshoes.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileManager {

    /**
     * VALID SAMPLES: 16bit Signed
     */
    fun copySamples(context: Context) {
        copy(DRUM_LOOP_1.first, DRUM_LOOP_1.second, context)
        copy(TERMINUS_SIGNED_16BIT_PCM_MONO.first, TERMINUS_SIGNED_16BIT_PCM_MONO.second, context)
    }

    private fun copy(res: Int, name: String, context: Context) {

        val file = File(context.filesDir, name)

        if (!file.exists()) {
            copyFile(res, context, file)
        }
    }

    private fun copyFile(res: Int, context: Context, file: File) {
        val inputStream: InputStream = context.resources.openRawResource(res)
        val out = FileOutputStream(file)
        try {
            var read: Int
            val buff = ByteArray(BUFFER_SIZE)

            while (inputStream.read(buff).also { read = it } > 0) {
                out.write(buff, 0, read)
            }
        } finally {
            inputStream.close()
            out.close()
        }
    }

    fun resolve(name: String, context: Context): String = File(context.filesDir, name).absolutePath

    companion object {
        val DRUM_LOOP_1 = R.raw.drum_loop_1 to "drum_loop_1.wav"
        val TERMINUS_SIGNED_16BIT_PCM_MONO = R.raw.terminus_signed_16bit_pcm_mono to "terminus_signed_16bit_pcm_mono.wav"

        private const val BUFFER_SIZE = 1024
    }
}
