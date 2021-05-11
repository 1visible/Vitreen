package c0d3.vitreen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Message
import c0d3.vitreen.app.models.dto.MessageDTO
import c0d3.vitreen.app.utils.FirestoreViewModel
import kotlinx.android.synthetic.main.discussion_item.view.*
import kotlinx.android.synthetic.main.message_item.view.*

class DiscussionAdapter(
    val lifecycle: LifecycleOwner,
    val fragment: Fragment,
    val senderId: String
) : ListAdapter<Message, DiscussionAdapter.DiscussionViewHolder>(DiscussionDiffCallback) {

    class DiscussionViewHolder(
        itemView: View,
        val lifecycle: LifecycleOwner,
        val fragment: Fragment,
        val senderId: String
    ) :
        RecyclerView.ViewHolder(itemView) {
        private var currentMessage: Message? = null

        private var viewModel: FirestoreViewModel =
            ViewModelProvider(fragment).get(FirestoreViewModel::class.java)


        fun bind(message: Message) {
            currentMessage = message
            if (currentMessage!!.senderId == senderId) {
                itemView.textViewMe.visibility = View.VISIBLE
                itemView.textViewMyContent.visibility = View.VISIBLE
                itemView.textViewMyDate.visibility = View.VISIBLE
                itemView.textViewOwnerName.visibility = View.GONE
                itemView.textViewOwnerContent.visibility = View.GONE
                itemView.textViewOwnerDate.visibility = View.GONE
                itemView.textViewMe.text = currentMessage!!.userName
                itemView.textViewMyContent.text = currentMessage!!.content
                itemView.textViewMyDate.text = currentMessage!!.date
            } else {
                itemView.textViewMe.visibility = View.GONE
                itemView.textViewMyContent.visibility = View.GONE
                itemView.textViewMyDate.visibility = View.GONE
                itemView.textViewOwnerName.visibility = View.VISIBLE
                itemView.textViewOwnerContent.visibility = View.VISIBLE
                itemView.textViewOwnerDate.visibility = View.VISIBLE
                itemView.textViewOwnerName.visibility = View.VISIBLE
                itemView.textViewOwnerContent.text = currentMessage!!.content
                itemView.textViewOwnerDate.text = currentMessage!!.date
            }
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.discussion_item, parent, false)
        return DiscussionAdapter.DiscussionViewHolder(view, lifecycle, fragment, senderId)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: DiscussionAdapter.DiscussionViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)

    }

}

object DiscussionDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.content == newItem.content
    }
}