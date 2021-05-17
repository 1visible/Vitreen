package c0d3.vitreen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Discussion
import kotlinx.android.synthetic.main.discussion_item.view.*
import kotlinx.android.synthetic.main.product_item.view.textViewTitle
import java.text.SimpleDateFormat
import java.util.*

class DiscussionAdapter(private val onClick: (Discussion) -> Unit):
    ListAdapter<Discussion, DiscussionAdapter.DiscussionViewHolder>(DiscussionDiffCallback) {

    class DiscussionViewHolder(itemView: View, val onClick: (Discussion) -> Unit): RecyclerView.ViewHolder(itemView) {
        private var discussion: Discussion = Discussion()

        init {
            itemView.setOnClickListener {
                onClick(discussion)
            }
        }

        fun bind(discussion: Discussion) {
            this.discussion = discussion

            val dateFormat = SimpleDateFormat(itemView.context.getString(R.string.date_format), Locale.getDefault())
            var date = dateFormat.format(Calendar.getInstance().time)
            var content = itemView.context.getString(R.string.no_message)

            if(discussion.messages.isNotEmpty()){
                val message = discussion.messages.last()
                date = dateFormat.format(message.date)
                content = message.content
            }

            // Fill discussion with informations
            itemView.textViewTitle.text = itemView.context.getString(R.string.about_product, discussion.productName)
            itemView.textViewDate.text = date
            itemView.textViewLastMessage.text = content
        }

    }

    // Create and inflate view and return DiscussionViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.discussion_item, parent, false)
        return DiscussionViewHolder(view, onClick)
    }

    // Get current discussion and use it to bind view.
    override fun onBindViewHolder(holder: DiscussionViewHolder, position: Int) {
        val discussion = getItem(position)
        holder.bind(discussion)

    }

}

object DiscussionDiffCallback : DiffUtil.ItemCallback<Discussion>() {
    override fun areItemsTheSame(oldItem: Discussion, newItem: Discussion): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Discussion, newItem: Discussion): Boolean {
        return oldItem.id == newItem.id
    }
}