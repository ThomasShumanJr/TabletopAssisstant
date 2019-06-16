package com.deadmandev.tabletopassisstant.services

import com.deadmandev.tabletopassisstant.controllers.App

object UserDataService {
    var id = ""
    var avatarName = ""
    var email = ""
    var name = ""

    fun logout() {
        id = ""
        avatarName = ""
        email = ""
        name = ""
        App.sharedPreferences.authToken = ""
        App.sharedPreferences.isLoggedIn = false
        App.sharedPreferences.userEmail = ""
        MessageService.clearMessages()
        MessageService.clearChannels()
    }