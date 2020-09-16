package com.slambang.shakeyshoes.oboe

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileManager {

    /**
     * VALID SAMPLES: 16bit Signed
     */
    fun copy(inputStream: InputStream, filesDIr: File): String {

        val tempFile = File(filesDIr, TEMP_AUDIO_FILE)
        if (tempFile.exists()) {
            tempFile.delete()
        }

        copyFile(inputStream, tempFile)

        return tempFile.absolutePath
    }

    private fun copyFile(inputStream: InputStream, file: File) {
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
        private const val BUFFER_SIZE = 1024
        private const val TEMP_AUDIO_FILE = "temp_audio_file.wav"
    }
}
