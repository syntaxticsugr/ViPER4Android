package com.llsl.viper4android.domain.preset

import android.content.Context
import android.net.Uri
import com.llsl.viper4android.audio.ViperParams
import com.llsl.viper4android.data.model.Preset
import com.llsl.viper4android.data.repository.ViperRepository
import com.llsl.viper4android.domain.file.AudioFileManager
import com.llsl.viper4android.utils.FileLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles importing presets, convolver kernels, and VDC files from picked content URIs:
 * reading the bytes, telling legacy XML presets apart from JSON ones, saving presets
 * through [ViperRepository], and reporting progress.
 *
 * The XML-to-JSON conversion is passed in as a function rather than called directly, since
 * the converter is a UI-package type and this class stays out of the `ui` package.
 * Notifications, toasts, and applying the imported state live in the caller and are reached
 * through callbacks.
 *
 * Each `import*` function runs synchronously on whatever coroutine context the caller uses;
 * callers are expected to launch it on `Dispatchers.IO`.
 */
@Singleton
class PresetImportManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val repository: ViperRepository,
        private val fileManager: AudioFileManager,
    ) {
        /**
         * Import preset files, either legacy `.xml` or the app's own `.json`. Each preset
         * that parses successfully is saved, updating an existing one when the name and
         * fx type already match.
         *
         * @param xmlToJson converts raw XML bytes plus the file name into the app's preset
         *   JSON, or null if the conversion fails.
         * @param isSpeakerXml decides headphone vs speaker for a raw XML preset.
         * @param onProgress reports `(current, total)` so the caller can drive a progress
         *   indicator; only fired for large batches, and the caller decides whether to show it.
         * @param onSinglePresetImported fires with `(json, fxType)` when exactly one preset
         *   was imported, giving the caller a chance to apply it as the live state.
         * @return how many presets were imported successfully.
         */
        suspend fun importPresets(
            uris: List<Uri>,
            xmlToJson: (raw: ByteArray, fileName: String) -> String?,
            isSpeakerXml: (raw: ByteArray, fileName: String) -> Boolean,
            onProgress: (current: Int, total: Int) -> Unit,
            onSinglePresetImported: suspend (json: String, fxType: Int) -> Unit,
        ): Int {
            val total = uris.size
            val showProgress = total > 10
            val destDir = fileManager.getFilesDir("Preset")
            var count = 0
            var lastJson: String? = null
            var lastFxType: Int = ViperParams.FX_TYPE_HEADPHONE
            for ((index, uri) in uris.withIndex()) {
                try {
                    val destFile =
                        if (uri.toString().endsWith(".xml", true)) {
                            val raw =
                                context
                                    .contentResolver
                                    .openInputStream(uri)
                                    ?.use { it.readBytes() }
                                    ?: throw Exception("Failed to read XML preset")
                            val fileName = uri.lastPathSegment ?: "preset.xml"
                            // xmlToJson already works out headphone vs speaker on its own,
                            // so the import path doesn't call isSpeakerXml here; that hook is
                            // exposed for callers that need the same decision without converting.
                            val json =
                                xmlToJson(raw, fileName) ?: throw Exception("Failed to convert XML preset")
                            val presetName = uri.path?.substringAfterLast("/") ?: "import_$index.xml"
                            val destFile = File(destDir, presetName.replace(".xml", ".json"))
                            FileOutputStream(destFile).use { fos ->
                                fos.write(json.toByteArray(Charsets.UTF_8))
                                fos.fd.sync()
                            }
                            destFile
                        } else {
                            fileManager.copyUriToFile(uri, destDir, "import_$index.json")
                        }
                    if (destFile != null) {
                        val json = destFile.readText()
                        val obj = JSONObject(json)
                        val isSpk = obj.has("spkMasterEnabled") && !obj.has("masterEnabled")
                        val fxType =
                            if (isSpk) ViperParams.FX_TYPE_SPEAKER else ViperParams.FX_TYPE_HEADPHONE
                        val presetName = destFile.nameWithoutExtension
                        val existing = repository.getPresetByNameAndFxType(presetName, fxType)
                        if (existing != null) {
                            repository.updatePreset(
                                existing.copy(settingsJson = json, updatedAt = System.currentTimeMillis()),
                            )
                        } else {
                            repository.savePreset(
                                Preset(
                                    name = presetName,
                                    fxType = fxType,
                                    settingsJson = json,
                                ),
                            )
                        }
                        count++
                        lastJson = json
                        lastFxType = fxType
                    }
                } catch (e: Exception) {
                    FileLogger.e("ViewModel", "Failed to import preset from $uri", e)
                }
                if (showProgress && ((index + 1) % 5 == 0 || index + 1 == total)) {
                    onProgress(index + 1, total)
                }
            }
            if (total == 1 && count == 1 && lastJson != null) {
                onSinglePresetImported(lastJson, lastFxType)
            }
            return count
        }

        /**
         * Import convolver kernel files into the Kernel dir.
         * @return the number of kernels successfully copied.
         */
        suspend fun importKernels(
            uris: List<Uri>,
            onProgress: (current: Int, total: Int) -> Unit,
        ): Int {
            val total = uris.size
            val showProgress = total > 50
            val destDir = fileManager.getFilesDir("Kernel")
            var count = 0
            for ((index, uri) in uris.withIndex()) {
                try {
                    if (fileManager.copyUriToFile(uri, destDir, "kernel_$count.wav") != null) count++
                } catch (e: Exception) {
                    FileLogger.e("ViewModel", "Failed to import kernel from $uri", e)
                }
                if (showProgress && ((index + 1) % 10 == 0 || index + 1 == total)) {
                    onProgress(index + 1, total)
                }
            }
            return count
        }

        /**
         * Import VDC (DDC) files into the DDC dir.
         * @return the number of VDCs successfully copied.
         */
        suspend fun importVdcs(
            uris: List<Uri>,
            onProgress: (current: Int, total: Int) -> Unit,
        ): Int {
            val total = uris.size
            val showProgress = total > 50
            val destDir = fileManager.getFilesDir("DDC")
            var count = 0
            for ((index, uri) in uris.withIndex()) {
                try {
                    if (fileManager.copyUriToFile(uri, destDir, "imported_$count.vdc") != null) count++
                } catch (e: Exception) {
                    FileLogger.e("ViewModel", "Failed to import VDC from $uri", e)
                }
                if (showProgress && ((index + 1) % 10 == 0 || index + 1 == total)) {
                    onProgress(index + 1, total)
                }
            }
            return count
        }
    }
