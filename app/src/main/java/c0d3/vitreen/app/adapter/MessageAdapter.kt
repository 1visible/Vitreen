package c0d3.vitreen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Message
import c0d3.vitreen.app.utils.Constants.Companion.MESSAGE_RECEIVED
import c0d3.vitreen.app.utils.Constants.Companion.MESSAGE_SENT
import kotlinx.android.synthetic.main.discussion_item.view.textViewDate
import kotlinx.android.synthetic.main.received_message_item.view.*
import kotlinx.android.synthetic.main.sent_message_item.view.textViewMessage
import java.text.SimpleDateFormat
import java.util.*


class MessageAdapter(private val userId: String, private val messages: List<Message>)
    : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback) {

    class SentMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var message: Message = Message()

        fun bind(message: Message) {
            this.message = message

            val dateFormat = SimpleDateFormat(itemView.context.getString(R.string.date_format), Locale.getDefault())

            itemView.textViewDate.text = dateFormat.format(message.date)
            itemView.textViewMessage.text = message.content
        }
    }

    class ReceivedMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var message: Message = Message()

        fun bind(message: Message) {
            this.message = message

            val dateFormat = SimpleDateFormat(itemView.context.getString(R.string.date_format), Locale.getDefault())

            itemView.textViewUsername.text = message.senderName
            itemView.textViewDate.text = dateFormat.format(message.date)
            itemView.textViewMessage.text = message.content
        }
    }

    // Create and inflate view and return MessageViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == MESSAGE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.sent_message_item, parent, false)

            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.received_message_item, parent, false)

            ReceivedMessageViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(messages[position].senderId == userId)
            MESSAGE_SENT
        else
            MESSAGE_RECEIVED
    }

    // Get current message and use it to bind view.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)

        if(holder.itemViewType == MESSAGE_SENT)
            (holder as SentMessageViewHolder).bind(message)
        else
            (holder as ReceivedMessageViewHolder).bind(message)
    }
}

object MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.content == newItem.content && oldItem.date == newItem.date
    }
}