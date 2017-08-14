package com.egoriku.catsrunning

import android.app.Activity
import android.app.Application
import android.os.Bundle

import com.egoriku.catsrunning.ui.activity.TracksActivity
import com.egoriku.core_lib.SimpleActivityLifecycleCallback

import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.CrashManagerListener
import net.hockeyapp.android.UpdateManager

object DebugInitializer {

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallback() {
            override fun onActivityCreated(activity: Activity?, bundle: Bundle?) {
                if (activity is TracksActivity) {
                    if (BuildConfig.DEBUG) {
                        CrashManager.register(activity, crashListener)
                        UpdateManager.register(activity)
                    } else {
                        CrashManager.register(activity, crashListener)
                    }
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (BuildConfig.DEBUG && activity is TracksActivity) {
                    UpdateManager.unregister()
                }
            }
        })
    }

    private val crashListener: CrashManagerListener
        get() = object : CrashManagerListener() {
            override fun shouldAutoUploadCrashes() = true
        }
}
