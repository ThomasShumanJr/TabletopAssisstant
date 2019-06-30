package com.deadmandev.tabletopassisstant.utilities


const val BASE_URL = "https://tabletopassisstant.herokuapp.com/v1/"
const val SOCKET_URL = "http://tabletopassisstant.herokuapp.com/"
const val URL_REGISTER = "${BASE_URL}account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_CREATE_USER = "${BASE_URL}user/add"
const val URL_GET_USER = "${BASE_URL}user/byEmail/"
const val URL_GET_CHANNELS = "${BASE_URL}channel/"
const val URL_GET_MESSAGES = "${BASE_URL}message/byChannel/"

//Broadcast constants
const val BROADCAST_USER_DETAIL_CHANGE = "BROADCAST_USER_DETAIL_CHANGE"