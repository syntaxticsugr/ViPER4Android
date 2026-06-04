package com.llsl.viper4android.domain.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.llsl.viper4android.audio.ByteArrayParam
import com.llsl.viper4android.audio.ParamEntry
import com.llsl.viper4android.audio.ViperParams
import com.llsl.viper4android.domain.audio.AudioParameterGateway
import com.llsl.viper4android.utils.FileLogger
import com.llsl.viper4android.utils.RootShell
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import javax.inject.Inject
import javax.inject.Singleton

/**
 * All file-system and root I/O for the app's audio assets: resolving and creating the
 * scratch directories, copying picked files in, decoding WAV kernels, building the DDC and
 * convolver byte-arrays that get sent to the engine, and listing/loading/deleting VDC and
 * kernel files.
 *
 * It holds no UI state. Methods that need to talk to the audio engine take an
 * [AudioParameterGateway] (plus the active fx type and AIDL flag) as arguments rather than
 * reaching for it directly, so this class stays purely about files.
 */
@Singleton
class AudioFileManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /** Resolve (and create) a sub-directory under the app's external files dir. */
        fun getFilesDir(subDir: String): File {
            val dir = File(context.getExternalFilesDir(null), subDir)
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

        /** Copy a content [uri] into [destDir], using its display name or [fallbackName]. */
        fun copyUriToFile(
            uri: Uri,
            destDir: File,
            fallbackName: String,
        ): File? {
            val fileName =
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
                } ?: fallbackName
            val destFile = File(destDir, fileName)
            return try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                        output.fd.sync()
                    }
                }
                destFile
            } catch (e: Exception) {
                FileLogger.e("ViewModel", "Failed to copy file", e)
                null
            }
        }

        fun getWavChannelCount(wavBytes: ByteArray): Int {
            if (wavBytes.size < 44) return 1
            val buf = ByteBuffer.wrap(wavBytes).order(ByteOrder.LITTLE_ENDIAN)
            buf.position(22)
            return buf.short.toInt()
        }

        fun decodeWavToFloat(wavBytes: ByteArray): FloatArray? {
            if (wavBytes.size < 44) return null
            val buf = ByteBuffer.wrap(wavBytes).order(ByteOrder.LITTLE_ENDIAN)

            val riff = ByteArray(4)
            buf.get(riff)
            if (String(riff) != "RIFF") return null
            buf.int
            val wave = ByteArray(4)
            buf.get(wave)
            if (String(wave) != "WAVE") return null

            var audioFormat = 0
            var numChannels = 0
            var bitsPerSample = 0
            var dataBytes: ByteArray? = null

            while (buf.remaining() >= 8) {
                val chunkId = ByteArray(4)
                buf.get(chunkId)
                val chunkSize = buf.int
                val chunkIdStr = String(chunkId)

                when (chunkIdStr) {
                    "fmt " -> {
                        val fmtStart = buf.position()
                        audioFormat = buf.short.toInt() and 0xFFFF
                        numChannels = buf.short.toInt() and 0xFFFF
                        buf.int
                        buf.int
                        buf.short
                        bitsPerSample = buf.short.toInt() and 0xFFFF
                        buf.position(fmtStart + chunkSize)
                    }

                    "data" -> {
                        val safeSize = minOf(chunkSize, buf.remaining())
                        dataBytes = ByteArray(safeSize)
                        buf.get(dataBytes)
                    }

                    else -> {
                        val skip = minOf(chunkSize, buf.remaining())
                        buf.position(buf.position() + skip)
                    }
                }
            }

            val data = dataBytes ?: return null
            if (numChannels !in 1..2) return null

            val dataBuf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val bytesPerSample = bitsPerSample / 8
            if (bytesPerSample == 0) return null
            val totalSamples = data.size / bytesPerSample
            val result = FloatArray(totalSamples)

            when {
                audioFormat == 1 && bitsPerSample == 16 -> {
                    for (i in 0 until totalSamples) result[i] = dataBuf.short.toFloat() / (1 shl 15)
                }

                audioFormat == 1 && bitsPerSample == 24 -> {
                    for (i in 0 until totalSamples) {
                        val b0 = dataBuf.get().toInt() and 0xFF
                        val b1 = dataBuf.get().toInt() and 0xFF
                        val b2 = dataBuf.get().toInt()
                        result[i] = ((b2 shl 16) or (b1 shl 8) or b0).toFloat() / (1 shl 23)
                    }
                }

                audioFormat == 1 && bitsPerSample == 32 -> {
                    for (i in 0 until totalSamples) result[i] = dataBuf.int.toFloat() / (1L shl 31)
                }

                audioFormat == 3 && bitsPerSample == 32 -> {
                    for (i in 0 until totalSamples) result[i] = dataBuf.float
                }

                else -> {
                    return null
                }
            }

            return result
        }

        /**
         * Read a VDC file and pack its 44.1 kHz and 48 kHz coefficient sets into the
         * byte-array layout the engine expects, ready to be included in a full-state
         * dispatch. Returns null if the file is missing or its coefficients don't line up.
         * DDC always targets the headphone path.
         */
        fun prepareDdcByteArray(name: String): ByteArrayParam? {
            return try {
                val file = File(getFilesDir("DDC"), "$name.vdc")
                FileLogger.i(
                    "ViewModel",
                    "prepareDdc: file=${file.absolutePath} exists=${file.exists()}",
                )
                if (!file.exists()) return null
                val lines = file.readLines()
                var coeffs44100: FloatArray? = null
                var coeffs48000: FloatArray? = null
                for (line in lines) {
                    val trimmed = line.trim()
                    when {
                        trimmed.startsWith("SR_44100:") -> {
                            coeffs44100 =
                                trimmed
                                    .removePrefix("SR_44100:")
                                    .split(",")
                                    .map { it.trim().toFloat() }
                                    .toFloatArray()
                        }

                        trimmed.startsWith("SR_48000:") -> {
                            coeffs48000 =
                                trimmed
                                    .removePrefix("SR_48000:")
                                    .split(",")
                                    .map { it.trim().toFloat() }
                                    .toFloatArray()
                        }
                    }
                }
                if (coeffs44100 == null || coeffs48000 == null) return null
                if (coeffs44100.size != coeffs48000.size) return null
                if (coeffs44100.size % 5 != 0) return null
                val arrSize = coeffs44100.size
                val naturalSize = 4 + arrSize * 4 * 2
                val wireSize =
                    when {
                        naturalSize <= 256 -> 256
                        naturalSize <= 1024 -> 1024
                        else -> return null
                    }
                val buffer = ByteBuffer.allocate(wireSize).order(ByteOrder.LITTLE_ENDIAN)
                buffer.putInt(arrSize)
                for (f in coeffs44100) buffer.putFloat(f)
                for (f in coeffs48000) buffer.putFloat(f)
                ByteArrayParam(ViperParams.PARAM_HP_DDC_COEFFICIENTS, buffer.array())
            } catch (e: Exception) {
                FileLogger.e("ViewModel", "Failed to prepare DDC: $name", e)
                null
            }
        }

        /**
         * Copy the kernel into the shared /data/local/tmp directory (using root) and build
         * the byte-array that points the engine at that path, ready for a full-state
         * dispatch. This file-path approach only applies in AIDL mode; in any other mode it
         * returns null and the kernel is streamed inline instead.
         */
        fun prepareConvolverByteArray(
            fileName: String,
            fxType: Int,
            aidlMode: Boolean,
        ): ByteArrayParam? {
            if (!aidlMode) return null
            return try {
                val file = File(getFilesDir("Kernel"), fileName)
                FileLogger.i(
                    "ViewModel",
                    "prepareConvolver: file=${file.absolutePath} exists=${file.exists()}",
                )
                if (!file.exists()) return null
                val safeName = fileName.replace("'", "")
                val subDir = if (fxType == ViperParams.FX_TYPE_SPEAKER) "spk" else "hp"
                val kernelPath = "/data/local/tmp/v4a/$subDir/$safeName"
                RootShell.copyFile(file, kernelPath)
                FileLogger.i("ViewModel", "Kernel copied to $kernelPath (for full state)")
                val param =
                    if (fxType == ViperParams.FX_TYPE_SPEAKER) {
                        ViperParams.PARAM_SPK_CONVOLVER_SET_KERNEL
                    } else {
                        ViperParams.PARAM_HP_CONVOLVER_SET_KERNEL
                    }
                val pathBytes = kernelPath.toByteArray(Charsets.UTF_8)
                val buffer = ByteBuffer.allocate(256).order(ByteOrder.LITTLE_ENDIAN)
                buffer.putInt(pathBytes.size)
                buffer.put(pathBytes)
                ByteArrayParam(param, buffer.array())
            } catch (e: Exception) {
                FileLogger.e("ViewModel", "Failed to prepare kernel: $fileName", e)
                null
            }
        }

        /** List importable VDC (DDC) names, sorted (no extension). */
        fun listVdcNames(): List<String> =
            getFilesDir("DDC")
                .listFiles()
                ?.filter { it.extension == "vdc" }
                ?.map { it.nameWithoutExtension }
                ?.sorted() ?: emptyList()

        /** List importable convolver kernel file names, sorted. */
        fun listKernelNames(): List<String> =
            getFilesDir("Kernel")
                .listFiles()
                ?.map { it.name }
                ?.sorted() ?: emptyList()

        /** Delete a VDC file; returns true if it existed and was removed. */
        fun deleteVdcFile(name: String): Boolean {
            return try {
                val file = File(getFilesDir("DDC"), "$name.vdc")
                if (!file.exists()) return false
                file.delete()
                true
            } catch (e: Exception) {
                FileLogger.e("ViewModel", "Failed to delete VDC: $name", e)
                false
            }
        }

        /** Delete a kernel file; returns true if it existed and was removed. */
        fun deleteKernelFile(fileName: String): Boolean {
            return try {
                val file = File(getFilesDir("Kernel"), fileName)
                if (!file.exists()) return false
                file.delete()
                true
            } catch (e: Exception) {
                FileLogger.e("ViewModel", "Failed to delete kernel: $fileName", e)
                false
            }
        }

        /**
         * Load a DDC file by name and dispatch its coefficients via [gateway].
         * Returns false if the file is missing/invalid or no service is bound.
         */
        fun loadVdcByName(
            name: String,
            gateway: AudioParameterGateway,
            enableParam: Int? = null,
        ): Boolean {
            FileLogger.i("ViewModel", "Loading DDC: $name")
            return try {
                val file = File(getFilesDir("DDC"), "$name.vdc")
                if (!file.exists()) return false
                val lines = file.readLines()

                var coeffs44100: FloatArray? = null
                var coeffs48000: FloatArray? = null

                for (line in lines) {
                    val trimmed = line.trim()
                    when {
                        trimmed.startsWith("SR_44100:") -> {
                            coeffs44100 =
                                trimmed
                                    .removePrefix("SR_44100:")
                                    .split(",")
                                    .map { it.trim().toFloat() }
                                    .toFloatArray()
                        }

                        trimmed.startsWith("SR_48000:") -> {
                            coeffs48000 =
                                trimmed
                                    .removePrefix("SR_48000:")
                                    .split(",")
                                    .map { it.trim().toFloat() }
                                    .toFloatArray()
                        }
                    }
                }

                if (coeffs44100 == null || coeffs48000 == null) return false
                if (coeffs44100.size != coeffs48000.size) return false
                if (coeffs44100.size % 5 != 0) return false

                val arrSize = coeffs44100.size
                val naturalSize = 4 + arrSize * 4 * 2
                val wireSize =
                    when {
                        naturalSize <= 256 -> 256
                        naturalSize <= 1024 -> 1024
                        else -> return false
                    }
                val buffer = ByteBuffer.allocate(wireSize).order(ByteOrder.LITTLE_ENDIAN)
                buffer.putInt(arrSize)
                for (f in coeffs44100) buffer.putFloat(f)
                for (f in coeffs48000) buffer.putFloat(f)

                if (gateway.boundService == null) return false
                val extras =
                    if (enableParam != null) listOf(ParamEntry(enableParam, intArrayOf(1))) else null
                gateway.dispatchParam(ViperParams.PARAM_HP_DDC_COEFFICIENTS, buffer.array(), extras)
                true
            } catch (e: Exception) {
                FileLogger.e("ViewModel", "Failed to load VDC: $name", e)
                false
            }
        }

        /**
         * Load a convolver kernel by name and send it to the engine through [gateway]. In
         * AIDL mode it hands the engine a file path via [loadKernelViaFile]; otherwise it
         * decodes the WAV to floats and streams the buffer in chunks. Returns false on any
         * failure or when no service is bound.
         */
        fun loadKernelByName(
            fileName: String,
            gateway: AudioParameterGateway,
            fxType: Int,
            aidlMode: Boolean,
            enableParam: Int? = null,
        ): Boolean {
            FileLogger.i("ViewModel", "Loading convolver kernel: $fileName")
            return try {
                val file = File(getFilesDir("Kernel"), fileName)
                if (!file.exists()) return false

                if (aidlMode) {
                    return loadKernelViaFile(file, fileName, gateway, fxType, enableParam)
                }

                val wavBytes = file.readBytes()
                val floatSamples = decodeWavToFloat(wavBytes) ?: return false
                val channelCount = getWavChannelCount(wavBytes)
                val totalFloats = floatSamples.size
                FileLogger.i(
                    "ViewModel",
                    "Kernel loaded: $fileName samples=$totalFloats ch=$channelCount",
                )

                if (gateway.boundService == null) return false

                val prepareParam =
                    if (fxType == ViperParams.FX_TYPE_SPEAKER) {
                        ViperParams.PARAM_SPK_CONVOLVER_PREPARE_BUFFER
                    } else {
                        ViperParams.PARAM_HP_CONVOLVER_PREPARE_BUFFER
                    }
                val setParam =
                    if (fxType == ViperParams.FX_TYPE_SPEAKER) {
                        ViperParams.PARAM_SPK_CONVOLVER_SET_BUFFER
                    } else {
                        ViperParams.PARAM_HP_CONVOLVER_SET_BUFFER
                    }
                val commitParam =
                    if (fxType == ViperParams.FX_TYPE_SPEAKER) {
                        ViperParams.PARAM_SPK_CONVOLVER_COMMIT_BUFFER
                    } else {
                        ViperParams.PARAM_HP_CONVOLVER_COMMIT_BUFFER
                    }

                gateway.dispatchParam(prepareParam, totalFloats, channelCount, 0)

                val floatBytes = ByteBuffer.allocate(totalFloats * 4).order(ByteOrder.LITTLE_ENDIAN)
                for (f in floatSamples) floatBytes.putFloat(f)
                val rawBytes = floatBytes.array()

                val crc = CRC32()
                crc.update(rawBytes)
                val crcValue = crc.value.toInt()

                val maxFloatsPerChunk = 2046
                var offset = 0
                var chunkIndex = 0
                while (offset < totalFloats) {
                    val remaining = totalFloats - offset
                    val floatsInChunk = minOf(remaining, maxFloatsPerChunk)
                    val chunkByteCount = floatsInChunk * 4

                    val chunkBuffer = ByteBuffer.allocate(8192).order(ByteOrder.LITTLE_ENDIAN)
                    chunkBuffer.putInt(chunkIndex)
                    chunkBuffer.putInt(floatsInChunk)
                    chunkBuffer.put(rawBytes, offset * 4, chunkByteCount)

                    gateway.dispatchParam(setParam, chunkBuffer.array())
                    offset += floatsInChunk
                    chunkIndex++
                }

                val kernelId = fileName.hashCode()
                gateway.dispatchParam(commitParam, totalFloats, crcValue, kernelId)
                true
            } catch (e: Exception) {
                FileLogger.e("ViewModel", "Failed to load kernel: $fileName", e)
                false
            }
        }

        private fun loadKernelViaFile(
            file: File,
            fileName: String,
            gateway: AudioParameterGateway,
            fxType: Int,
            enableParam: Int? = null,
        ): Boolean {
            return try {
                val safeName = fileName.replace("'", "")
                val subDir = if (fxType == ViperParams.FX_TYPE_SPEAKER) "spk" else "hp"
                val kernelPath = "/data/local/tmp/v4a/$subDir/$safeName"
                RootShell.copyFile(file, kernelPath)
                FileLogger.i("ViewModel", "Kernel copied to $kernelPath")

                val param =
                    if (fxType == ViperParams.FX_TYPE_SPEAKER) {
                        ViperParams.PARAM_SPK_CONVOLVER_SET_KERNEL
                    } else {
                        ViperParams.PARAM_HP_CONVOLVER_SET_KERNEL
                    }
                val pathBytes = kernelPath.toByteArray(Charsets.UTF_8)
                val buffer = ByteBuffer.allocate(256).order(ByteOrder.LITTLE_ENDIAN)
                buffer.putInt(pathBytes.size)
                buffer.put(pathBytes)
                if (gateway.boundService == null) return false
                val extras =
                    if (enableParam != null) listOf(ParamEntry(enableParam, intArrayOf(1))) else null
                gateway.dispatchParam(param, buffer.array(), extras)
                true
            } catch (e: Exception) {
                FileLogger.e("ViewModel", "Failed to load kernel via file: $fileName", e)
                false
            }
        }
    }
