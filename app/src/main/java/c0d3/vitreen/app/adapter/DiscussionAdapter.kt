package c0d3.vitreen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.dto.DiscussionDTO
import kotlinx.android.synthetic.main.message_item.view.*
import kotlinx.android.synthetic.main.product_item.view.*

class DiscussionAdapter(private val onClick: (DiscussionDTO) -> Unit):
    ListAdapter<DiscussionDTO, DiscussionAdapter.DiscussionViewHolder>(DiscussionDiffCallback) {

    class DiscussionViewHolder(itemView: View, val onClick: (DiscussionDTO) -> Unit): RecyclerView.ViewHolder(itemView) {
        private var discussionDTO: DiscussionDTO = DiscussionDTO()

        init {
            itemView.setOnClickListener {
                onClick(discussionDTO)
            }
        }

        fun bind(discussion: DiscussionDTO) {
            discussionDTO = discussion
            // Fill product with informations
            itemView.textViewTitle.text = itemView.context.getString(R.string.about_product, discussionDTO.productName)
            itemView.textViewLastMessage.text = discussionDTO.lastMessage.content
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

object DiscussionDiffCallback : DiffUtil.ItemCallback<DiscussionDTO>() {
    override fun areItemsTheSame(oldItem: DiscussionDTO, newItem: DiscussionDTO): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DiscussionDTO, newItem: DiscussionDTO): Boolean {
        return oldItem.id == newItem.id
    }
}