package com.deadmandev.tabletopassisstant.adapters


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.deadmandev.tabletopassisstant.R
import com.deadmandev.tabletopassisstant.models.Message
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(val context: Context, val messages: ArrayList<Message>): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
        holder.bindMessage(context, messages[p1])
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val userImage = itemView.findViewById<ImageView>(R.id.userImage)!!
        private val timestamp = itemView.findViewById<TextView>(R.id.MessageDateTime)!!
        private val username = itemView.findViewById<TextView>(R.id.userNameText)!!
        private val message = itemView.findViewById<TextView>(R.id.message)!!

        fun bindMessage(context: Context, message: Message) {
            val resourceID = context.resources.getIdentifier(message.userAvatar,"drawable", context.packageName)
            userImage.setImageResource(resourceID)
            username.text = message.userName
            timestamp.text = message.timeStamp
            this.message.text = formatDateTime(message.timeStamp)
        }

        fun formatDateTime(isoString: String): String {
            val isoFormatted = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormatted.timeZone = TimeZone.getTimeZone("UTC")
            var convertedDate = Date()
            try {
                convertedDate = isoFormatted.parse(toString())
            } catch (e: ParseException) {
                Log.d("EXEC", "Cannot parse date ${e.localizedMessage}")
            }

            val outDate = SimpleDateFormat("E, h:mm a", Locale.getDefault())
            return outDate.format(convertedDate)
        }
    }
}