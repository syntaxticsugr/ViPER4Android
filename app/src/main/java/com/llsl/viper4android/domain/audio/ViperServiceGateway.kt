package com.llsl.viper4android.domain.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.llsl.viper4android.audio.ParamEntry
import com.llsl.viper4android.service.ViperService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [AudioParameterGateway] backed by a real bound [ViperService].
 *
 * Owns the [ServiceConnection] lifecycle and forwards each dispatch call to the connected
 * service. When nothing is bound the service reference is null and every dispatch quietly
 * does nothing, so callers don't have to guard against an unbound state themselves.
 */
@Singleton
class ViperServiceGateway
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : AudioParameterGateway {
        private var service: ViperService? = null
        private var serviceBound = false
        private var connectedCallback: (() -> Unit)? = null
        private var disconnectedCallback: (() -> Unit)? = null

        private val connection =
            object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName?,
                    binder: IBinder?,
                ) {
                    val localBinder = binder as? ViperService.LocalBinder ?: return
                    service = localBinder.service
                    serviceBound = true
                    connectedCallback?.invoke()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    service = null
                    serviceBound = false
                    disconnectedCallback?.invoke()
                }
            }

        override val boundService: ViperService?
            get() = service

        override val isBound: Boolean
            get() = serviceBound

        override fun bind(
            onConnected: () -> Unit,
            onDisconnected: () -> Unit,
        ) {
            connectedCallback = onConnected
            disconnectedCallback = onDisconnected
            val intent = Intent(context, ViperService::class.java)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        override fun unbind() {
            if (serviceBound) {
                context.unbindService(connection)
                serviceBound = false
            }
            service = null
        }

        override fun dispatchParam(
            param: Int,
            value: Int,
        ) {
            service?.dispatchParam(param, value)
        }

        override fun dispatchParam(
            param: Int,
            val1: Int,
            val2: Int,
            val3: Int,
        ) {
            service?.dispatchParam(param, val1, val2, val3)
        }

        override fun dispatchParam(
            param: Int,
            value: ByteArray,
            extraParams: List<ParamEntry>?,
        ) {
            service?.dispatchParam(param, value, extraParams)
        }

        override fun dispatchParamsBatch(entries: List<ParamEntry>) {
            service?.dispatchParamsBatch(entries)
        }

        override fun dispatchEqBands(
            param: Int,
            bandsString: String,
            bandCountParam: Int,
            bandCount: Int,
        ) {
            service?.dispatchEqBands(param, bandsString, bandCountParam, bandCount)
        }

        override fun setEffectEnabled(enabled: Boolean) {
            service?.setEffectEnabled(enabled)
        }

        override fun setGlobalMode(enabled: Boolean) {
            service?.setGlobalMode(enabled)
        }
    }
