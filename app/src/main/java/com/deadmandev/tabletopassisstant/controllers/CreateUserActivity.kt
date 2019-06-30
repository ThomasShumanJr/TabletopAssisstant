package com.deadmandev.tabletopassisstant.controllers


import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.deadmandev.tabletopassisstant.R
import com.deadmandev.tabletopassisstant.services.AuthService
import com.deadmandev.tabletopassisstant.services.UserDataService
import com.deadmandev.tabletopassisstant.services.UserDataService.avatarName
import com.deadmandev.tabletopassisstant.utilities.BROADCAST_USER_DETAIL_CHANGE
import kotlinx.android.synthetic.main.content_create_user.*
import java.util.*

class  CreateUserActivity : AppCompatActivity() {

    private var userAvatar = "profileDefault"
    private var avatarColor = "[0.5, 0.5, 0.5, 1]"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        createUserSpinner.visibility = View.INVISIBLE
    }

    fun onCreateUserAccountButtonClick(view: View) {

        enableSpinner(true)
        val username = createUserUsernameText.text.toString()
        val email = createUserEmailText.text.toString()
        val password = createUserPasswordText.text.toString()

        if(username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()){

            AuthService.registerUser(email,
                password
            ) { complete ->
                if (complete) {
                    Toast.makeText( this,"Registration Successful", Toast.LENGTH_LONG).show()
                    AuthService.loginUser(email,
                        password
                    ) { loginSuccess ->
                        if (loginSuccess) {
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG).show()
                            AuthService.createUser( username, email, userAvatar, avatarColor) { createSuccess ->
                                if (createSuccess) {

                                    val userDataChange = Intent(BROADCAST_USER_DETAIL_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                                    println(UserDataService)
                                    enableSpinner(false)
                                    finish()
                                } else {
                                    errorToast()
                                }

                            }
                        } else {
                            errorToast()
                        }
                    }

                } else {
                    errorToast()
                }

            }
        }else{
            Toast.makeText(this, "Make sure user name, email and password are filled in", Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }
    }

    fun errorToast() {
        Toast.makeText(this, "Something went wrong...Please try again later", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun onGenerateUserAvatarClick(view: View) {
        val random = Random()
        val theme = random.nextInt(2)
        val avatar = random.nextInt(28)

        if(theme == 0) {
            this.userAvatar = "light$avatar"
        } else {
            this.userAvatar = "dark$avatar"
        }

        val resourceID = resources.getIdentifier(this.userAvatar, "drawable", packageName)
        createAvatarImageView.setImageResource(resourceID)
    }

    fun generateBackgroundColorClicked(view:View) {
        val random = Random()
        val r = random.nextInt(225)
        val g = random.nextInt(225)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r,g,b))

        val savedR = r.toDouble()/255
        val savedG = g.toDouble()/255
        val savedB = b.toDouble()/255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"

    }

    fun enableSpinner(enable: Boolean) {
        if(enable) {
            createUserSpinner.visibility = View.VISIBLE
        } else {
            createUserSpinner.visibility = View.INVISIBLE
        }
        createUserCreateAccountButton.isEnabled = !enable
        createAvatarImageView.isEnabled = !enable

    }
}