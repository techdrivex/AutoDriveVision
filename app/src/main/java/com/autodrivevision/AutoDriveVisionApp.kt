package com.autodrivevision

import android.app.Application
import android.content.Context
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

class AutoDriveVisionApp : Application(), CameraXConfig.Provider {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    companion object {
        private lateinit var instance: AutoDriveVisionApp

        fun getContext(): Context = instance.applicationContext
    }
} 