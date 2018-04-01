/*
 * Copyright (C) 2018 Mobile Application Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mobileapplab.library

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Handler.Callback
import android.os.HandlerThread
import android.os.Message
import android.util.Log

/**
 * A package observer is used to register callback that can be notified of package state changes in android device.
 */
abstract class PackageObserver(context: Context) {

    companion object {
        @JvmStatic
        private val DEBUG = BuildConfig.DEBUG

        @JvmStatic
        private val TAG = PackageObserver::javaClass.name
    }

    private val context = context.applicationContext

    private val handlerThread = HandlerThread("PackageObserver")

    private val callbackHandler: Handler

    private val receiver: BroadcastReceiver

    init {
        handlerThread.start()
        callbackHandler = Handler(handlerThread.looper, Callback {
            onPackageStateChanged(it.obj as PackageState)
            true
        })

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras ?: return
                if (DEBUG) {
                    bundle.let {
                        for (key in it.keySet()) {
                            val value = bundle.get(key)
                            value?.let {
                                Log.d(TAG, String.format("%s %s (%s)", key, it.toString(), value.javaClass.name))
                            }
                        }
                    }
                }

                var state: PackageState.State? = null

                when (intent.action) {
                    Intent.ACTION_PACKAGE_ADDED -> {
                        if (bundle.getBoolean(Intent.EXTRA_REPLACING)) {
                            // Updating
                        } else {
                            // Install
                            state = PackageState.State.INSTALLED
                        }
                    }

                    Intent.ACTION_PACKAGE_REPLACED -> {
                        // Updated
                        state = PackageState.State.UPDATED
                    }

                    Intent.ACTION_PACKAGE_REMOVED -> {
                        if (bundle.getBoolean(Intent.EXTRA_DATA_REMOVED)) {
                            if (bundle.getBoolean(Intent.EXTRA_REPLACING)) {
                                // Update removed
                                state = PackageState.State.UPDATE_REMOVED
                            } else {
                                // Removed
                                state = PackageState.State.REMOVED
                            }
                        } else {
                            if (bundle.getBoolean(Intent.EXTRA_REPLACING)) {
                                // Updating
                                state = PackageState.State.UPDATING
                            }
                        }
                    }

                    Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                        // Fully removed
                        state = PackageState.State.FULLY_REMOVED
                    }

                    Intent.ACTION_PACKAGE_CHANGED -> {
                        // Change enabled settings
                        state = PackageState.State.CHANGE_ENABLED_SETTINGS
                    }
                }

                state?.let {
                    val message = Message.obtain()
                    message.obj = PackageState(intent.data?.schemeSpecificPart.orEmpty(), it)
                    if (DEBUG) {
                        Log.d(TAG, "Sending message: ${message.obj}")
                    }
                    callbackHandler.sendMessage(message)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        this.context.registerReceiver(receiver, filter)
    }

    /**
     * This function to be invoked when the package state changed.
     *
     * @param packageState the changed package state.
     */
    abstract fun onPackageStateChanged(packageState: PackageState)

    /**
     * Stop observing package state and release the instance.
     */
    fun release() {
        context.unregisterReceiver(receiver)
        handlerThread.quit()
    }

}