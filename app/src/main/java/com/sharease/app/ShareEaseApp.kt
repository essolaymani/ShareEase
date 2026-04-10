package com.sharease.app

import android.app.Application

class ShareEaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ShareEaseApp
            private set
    }
}
