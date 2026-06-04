package com.llsl.viper4android.domain.audio

import com.llsl.viper4android.audio.ParamEntry
import com.llsl.viper4android.service.ViperService

/**
 * Thin abstraction over the bound [ViperService] for sending audio parameters to it.
 *
 * Its job is to keep the service-connection lifecycle and the low-level dispatch calls out
 * of the ViewModel. Anything that needs UI-state types (such as pushing a full effect state
 * or reading back the active effect) stays in the caller, which can still reach the live
 * service through [boundService]. That separation keeps this domain layer from depending on
 * anything in the `ui` package.
 */
interface AudioParameterGateway {
    /** The currently-bound service instance, or null when not connected. */
    val boundService: ViperService?

    /** True once [bind] has connected and the service is available. */
    val isBound: Boolean

    /**
     * Bind to [ViperService]. [onConnected] runs on the main thread once the service is
     * available; [onDisconnected] runs when the connection drops.
     */
    fun bind(
        onConnected: () -> Unit,
        onDisconnected: () -> Unit,
    )

    /** Unbind from the service if currently bound. */
    fun unbind()

    fun dispatchParam(
        param: Int,
        value: Int,
    )

    fun dispatchParam(
        param: Int,
        val1: Int,
        val2: Int,
        val3: Int,
    )

    fun dispatchParam(
        param: Int,
        value: ByteArray,
        extraParams: List<ParamEntry>? = null,
    )

    fun dispatchParamsBatch(entries: List<ParamEntry>)

    fun dispatchEqBands(
        param: Int,
        bandsString: String,
        bandCountParam: Int = 0,
        bandCount: Int = 0,
    )

    /** Enable/disable the active effect(s). */
    fun setEffectEnabled(enabled: Boolean)

    /** Toggle global (per-app vs system-wide) processing mode on the service. */
    fun setGlobalMode(enabled: Boolean)
}
