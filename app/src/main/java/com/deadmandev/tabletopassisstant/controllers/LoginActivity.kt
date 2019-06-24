package com.deadmandev.tabletopassisstant.controllers
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.Request.Method.*
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.deadmandev.tabletopassisstant.controllers.App
import com.deadmandev.tabletopassisstant.utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    // var isLoggedIn = false
    // var userEmail = ""
    // var authToken = ""

    fun registerUser(email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        println(jsonBody)

        val requestBody = jsonBody.toString()
        val registerRequest = object: StringRequest(POST, URL_REGISTER, Response.Listener { response ->
            println(response)
            complete(true)
        }, Response.ErrorListener { error ->
            println(error)
            Log.d("Error", "Could not register user $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        App.sharedPreferences.requestQueue.add(registerRequest)
    }

    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)

        val requestBody = jsonBody.toString()

        val loginRequest = object: JsonObjectRequest(
            POST, URL_LOGIN, null, Response.Listener { response ->
                // this is where we parse the json object
                // println(response)
                try {
                    App.sharedPreferences.userEmail = response.getString("user")
                    App.sharedPreferences.authToken = response.getString("token")
                    App.sharedPreferences.isLoggedIn = true
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON error", "EXCEPTION ${e.localizedMessage}")
                    complete(false)
                }

            }, Response.ErrorListener { error ->
                // this is where we deal with our error
                Log.d("ERROR", "$error")
                complete(false)
            }) {

            override fun getBodyContentType(): String {
                return "application/json; charset=UTF-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        App.sharedPreferences.requestQueue.add(loginRequest)
    }

    fun createUser(name: String, email: String, avatarName: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatarName)

        val requestBody = jsonBody.toString()

        val createRequest = object : JsonObjectRequest(
            POST, URL_CREATE_USER, null, Response.Listener { response ->

                try {
                    UserDataService.name = response.getString("name")
                    UserDataService.email = response.getString("email")
                    UserDataService.avatarName = response.getString("avatarName")
                    UserDataService.id = response.getString("_id")
                    complete(true)

                } catch (e: JSONException){
                    Log.d("JSON Error:", e.localizedMessage)
                    complete(false)
                }
            }, Response.ErrorListener {error ->
                Log.d("ERROR", error.toString())
                complete(false)
            }) {

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.sharedPreferences.authToken}")
                return headers
            }
        }
        App.sharedPreferences.requestQueue.add(createRequest)
    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit) {
        val findUserRequest = object : JsonObjectRequest(
            GET, "$URL_GET_USER${App.sharedPreferences.userEmail}", null, Response.Listener { response ->
                try {
                    UserDataService.name = response.getString("name")
                    UserDataService.email = response.getString("email")
                    UserDataService.avatarName = response.getString("avatarName")
                    UserDataService.id = response.getString("_id")

                    val userDataChange = Intent(BROADCAST_USER_DETAIL_CHANGE)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                    complete(true)

                } catch (e: JSONException) {
                    Log.d("JSONException", e.localizedMessage)
                    complete(false)
                }

            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not find user $error")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.sharedPreferences.authToken}"
                return headers
            }
        }
        App.sharedPreferences.requestQueue.add(findUserRequest)
    }
