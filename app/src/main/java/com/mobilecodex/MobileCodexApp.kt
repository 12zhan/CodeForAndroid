package com.mobilecodex

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Mobile Codex Application
 * 使用 Hilt 进行依赖注入
 */
@HiltAndroidApp
class MobileCodexApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
