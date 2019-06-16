package com.deadmandev.tabletopassisstant.services

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.deadmandev.tabletopassisstant.controllers.App
import com.deadmandev.tabletopassisstant.models.Channel
import com.deadmandev.tabletopassisstant.models.Message
import com.deadmandev.tabletopassisstant.utilities.URL_GET_CHANNELS
import com.deadmandev.tabletopassisstant.utilities.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(complete: (Boolean) -> Unit) {

        val channelsRequest = object : JsonArrayRequest(Method.GET,
            URL_GET_CHANNELS, null, Response.Listener { response ->
            try {
                for (x in 0 until response.length()) {
                    val channel = response.getJSONObject(x)
                    val name = channel.getString("name")
                    val channelDescription = channel.getString("description")
                    val channelID = channel.getString("_id")

                    val newChannel = Channel(name, channelDescription, channelID)
                    this.channels.add(newChannel)
                }
                complete(true)
            } catch (e: JSONException) {
                Log.d("EXCEPTION", "JSON Exception ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener {error ->
            println(error)
            Log.d("ERROR", "Unable to get channels $error")
            complete(false)
        }) {

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.sharedPreferences.authToken}")
                return headers
            }
        }
        App.sharedPreferences.requestQueue.add(channelsRequest)
    }

    fun getMessages(channelID: String, complete: (Boolean) -> Unit) {
        val url = "$URL_GET_MESSAGES$channelID"
        clearMessages()
        val messagesRequest = object: JsonArrayRequest(Method.GET, url, null, Response.Listener {response ->
            try {
                for (x in 0 until response.length()){
                    val message = response.getJSONObject(x)
                    val messageBody = message.getString("messageBody")
                    val channelID = message.getString("channelId")
                    val id = message.getString("_id")
                    val userName = message.getString("userName")
                    val userAvatar = message.getString("userAvatar")
                    val timeStamp = message.getString("timeStamp")

                    val newMessage = Message(messageBody,
                        userName,
                        channelID,
                        userAvatar,
                        id,
                        timeStamp)

                    this.messages.add(newMessage)
                }
                complete(true)
            } catch (e: JSONException) {
                Log.d("EXCEPTION", "JSON Error: ${e.localizedMessage}")
                complete(false)
            }

        }, Response.ErrorListener {
            Log.d("ERROR", "Could no retrieve message")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.sharedPreferences.authToken}")
                return headers
            }
        }
        App.sharedPreferences.requestQueue.add(messagesRequest)
    }

    fun clearMessages() {
        messages.clear()
    }
    fun clearChannels() {
        channels.clear()
    }

}