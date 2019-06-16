package com.deadmandev.tabletopassisstant.controllers


import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.deadmandev.tabletopassisstant.R
import com.deadmandev.tabletopassisstant.services.AuthService
import com.deadmandev.tabletopassisstant.services.UserDataService
import com.deadmandev.tabletopassisstant.utilities.BROADCAST_USER_DETAIL_CHANGE
import kotlinx.android.synthetic.main.activity_sign_up_user.*
import java.util.*

class  SignUpUser : AppCompatActivity() {

    private var userAvatar = "profileDefault"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_user)

        createUserSpinner.visibility = View.INVISIBLE
    }

    fun onSignUpCreateAccountButtonClick(view: View) {
        enableSpinner(true)
        val username = SignUpUsernameInput.text.toString()
        val email = SignUpEmailInput.text.toString()
        val password = SignUpPasswordInput.text.toString()

        if(username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            AuthService.registerUser(
                email,
                password
            ) { registerSuccess ->
                if (registerSuccess) {
                    println("User registered successfully")
                    AuthService.loginUser(email, password) { loginSuccess ->
                        if (loginSuccess) {
                            println("User successfully logged in")
                            println(App.sharedPreferences.authToken)
                            println(App.sharedPreferences.userEmail)
                            AuthService.createUser( username, email, userAvatar) { createSuccess ->
                                if (createSuccess) {

                                    val userDataChange = Intent(BROADCAST_USER_DETAIL_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                                    println(UserDataService)
                                    enableSpinner(false)
                                    finish()
                                } else {
                                    println("User creation failed")
                                    errorToast()
                                }
                            }
                        } else {
                            println("User auth failed")
                            errorToast()
                        }
                    }
                } else {
                    println("User registration failed")
                    errorToast()
                }
            }
        } else {
            Toast.makeText(this, "Make sure username, email and password are not empty", Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }
    }

    fun errorToast() {
        Toast.makeText(this, "Something went wrong...Please try again later", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun onSignUpCreateAvatarImageButtonClick(view: View) {
        val random = Random()
        val theme = random.nextInt(2)
        val avatar = random.nextInt(28)

        if(theme == 0) {
            this.userAvatar = "light$avatar"
        } else {
            this.userAvatar = "dark$avatar"
        }

        val resourceID = resources.getIdentifier(this.userAvatar, "drawable", packageName)
        SignUpCreateAvatarImageButton.setImageResource(resourceID)
    }

    fun enableSpinner(enable: Boolean) {
        if(enable) {
            createUserSpinner.visibility = View.VISIBLE
        } else {
            createUserSpinner.visibility = View.INVISIBLE
        }
        SignUpCreateAccountButton.isEnabled = !enable
        SignUpCreateAvatarImageButton.isEnabled = !enable

    }
}