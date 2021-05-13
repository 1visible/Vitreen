package c0d3.vitreen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Message
import kotlinx.android.synthetic.main.discussion_item.view.*


class MessageAdapter(val senderId: String)
    : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback) {

    class MessageViewHolder(itemView: View, val senderId: String): RecyclerView.ViewHolder(itemView) {
        private var messageDTO: Message = Message()

        fun bind(message: Message) {
            messageDTO = message

            // TODO : Tout revoir
            itemView.textViewMe.text = messageDTO.content // TODO : Revoir ça
            itemView.textViewMyContent.text = messageDTO.content
            itemView.textViewMyDate.text = messageDTO.date.toString()
            itemView.textViewMe.visibility = VISIBLE
            itemView.textViewMyContent.visibility = VISIBLE
            itemView.textViewMyDate.visibility = VISIBLE
            itemView.textViewOwnerName.visibility = GONE
            itemView.textViewOwnerContent.visibility = GONE
            itemView.textViewOwnerDate.visibility = GONE
        }

    }

    // Create and inflate view and return MessageViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view, senderId)
    }

    // Get current message and use it to bind view.
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)

    }

}

object MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.content == newItem.content
    }
}