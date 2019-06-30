package com.deadmandev.tabletopassisstant.controllers

import android.content.*
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import com.deadmandev.tabletopassisstant.R
import com.deadmandev.tabletopassisstant.adapters.MessageAdapter
import com.deadmandev.tabletopassisstant.models.Channel
import com.deadmandev.tabletopassisstant.models.Message
import com.deadmandev.tabletopassisstant.services.AuthService
import com.deadmandev.tabletopassisstant.services.MessageService
import com.deadmandev.tabletopassisstant.services.UserDataService
import com.deadmandev.tabletopassisstant.utilities.BROADCAST_USER_DETAIL_CHANGE
import com.deadmandev.tabletopassisstant.utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity(){

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null
    private fun setupAdapters(){

        channelAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,MessageService.channels)
        channelList.adapter = channelAdapter

        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageList.adapter = messageAdapter

        val layoutManager = LinearLayoutManager(this)
        messageList.layoutManager = layoutManager

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)

        setupAdapters()

        channelList.setOnItemClickListener { _, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if( App.sharedPreferences.isLoggedIn) {
            AuthService.findUserByEmail(this){}
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(
            BROADCAST_USER_DETAIL_CHANGE)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        socket.disconnect()
    }
    private var userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if(App.sharedPreferences.isLoggedIn) {
                userNameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email
                val resourceID = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userImagaNavHeader.setImageResource(resourceID)
                loginButtonNavHeader.text = "LOGOUT"

                MessageService.getChannels{ complete ->
                    if(complete) {
                        if(MessageService.channels.count()>0) {
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun onLoginButtonClick(view: View){

        if(App.sharedPreferences.isLoggedIn) {
            // logout
            UserDataService.logout()
            channelAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()
            userNameNavHeader.text = R.string.log_in.toString()
            userEmailNavHeader.text = ""
            userImagaNavHeader.setImageResource(R.drawable.profiledefault)
            loginButtonNavHeader.text = R.string.log_in.toString()
            activeChannelName.text = R.string.please_log_in.toString()
        }
        else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun onAddChannelButtonClick(view: View){
        if(App.sharedPreferences.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                .setPositiveButton("Create Channel") { dialogInterface, i ->
                    // perform some logic when create button clicked
                    val channelName = dialogView.findViewById<EditText>(R.id.addChannelNameInput)
                    val channelDescription = dialogView.findViewById<EditText>(R.id.addChannelDescriptionInput)

                    val channelNameText = channelName.text.toString()
                    val channelDescriptionText = channelDescription.text.toString()

                    // Create a channel with channel Name and description
                    socket.emit("newChannel", channelNameText, channelDescriptionText)
                }
                .setNegativeButton("Cancel") { dialogInterface, i ->
                    // Cancel and close the dialog
                }
                .show()
        } else {
            Toast.makeText(this, "Please login to create a channel", Toast.LENGTH_LONG)
                .show()
        }
    }

    private val onNewChannel = Emitter.Listener { args ->
        if(App.sharedPreferences.isLoggedIn) {
            runOnUiThread {
                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelID = args[2] as String

                val channel = Channel(channelName, channelDescription, channelID)
                MessageService.channels.add(channel)
                println(channel)
                channelAdapter.notifyDataSetChanged()
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if(App.sharedPreferences.isLoggedIn) {
            runOnUiThread {
                val channelID = args[2] as String
                if (channelID == selectedChannel?.id) {
                    val messageBody = args[0] as String
                    val userNmae = args[3] as String
                    val userAvatar = args[4] as String
                    val id = args[5] as String
                    val timeStamp = args[6] as String

                    val newMessage = Message(
                        messageBody,
                        userNmae,
                        channelID,
                        userAvatar,
                        id,
                        timeStamp
                    )

                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListReview.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
    fun onSendMessageButtonClick(view: View){
        if(App.sharedPreferences.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {
            val userID = UserDataService.id
            val channelID = selectedChannel!!.id
            socket.emit("newMessage", messageTextField.text.toString(), userID, channelID,
                UserDataService.name, UserDataService.avatarName)
            messageTextField.text.clear()
            hideKeyboard()
        }

    }

    fun updateWithChannel() {
        activeChannelName.text = "#${selectedChannel?.name}"
        // download messages for channel
        if(selectedChannel != null) {
            MessageService.getMessages(selectedChannel!!.id) { complete ->
                if(complete) {
                    messageAdapter.notifyDataSetChanged()
                    if(messageAdapter.itemCount > 0) {
                        messageListReview.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }
    }
}