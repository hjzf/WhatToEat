package com.lkjhgfdsa

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class ThisApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        const val IMAGE_DIR = "food_images"
    }

    override fun onCreate() {
        super.onCreate()
        context = baseContext
    }

}