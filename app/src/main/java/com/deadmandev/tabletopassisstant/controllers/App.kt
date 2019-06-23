package com.deadmandev.tabletopassisstant.controllers

import android.app.Application
import com.deadmandev.tabletopassisstant.utilities.SharedPreferences

class App: Application() {

    companion object {
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate() {
        sharedPreferences = SharedPreferences(applicationContext)
        super.onCreate()
    }
}