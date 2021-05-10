package c0d3.vitreen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.dto.MessageDTO
import c0d3.vitreen.app.utils.FirestoreViewModel
import kotlinx.android.synthetic.main.message_item.view.*

class MessageAdapter(
    private val onClick: (MessageDTO) -> Unit,
    val lifecycle: LifecycleOwner,
    val fragment: Fragment
) :
    ListAdapter<MessageDTO, MessageAdapter.MessageViewHolder>(MessageDiffCallback) {
    class MessageViewHolder(
        itemView: View,
        val onClick: (MessageDTO) -> Unit,
        val lifecycle: LifecycleOwner,
        val fragment: Fragment
    ) :
        RecyclerView.ViewHolder(itemView) {
        private var currentMessage: MessageDTO? = null

        private var viewModel: FirestoreViewModel =
            ViewModelProvider(fragment).get(FirestoreViewModel::class.java)

        init {
            itemView.setOnClickListener {
                currentMessage.let {
                    if (it != null) {
                        onClick(it)
                    }
                }
            }
        }

        fun bind(message: MessageDTO) {
            currentMessage = message
            viewModel.getImages(currentMessage!!.productId, 1)
                .observe(lifecycle, { pair ->
                    itemView.imageViewMessage.setImageBitmap(pair.second.first())
                    itemView.textViewTitleMessage.text =
                        fragment.getString(R.string.about).plus(currentMessage!!.productName)
                    itemView.textViewLastMessage.text = currentMessage!!.lastMessage.content
                })
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view, onClick, lifecycle, fragment)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)

    }
}

object MessageDiffCallback : DiffUtil.ItemCallback<MessageDTO>() {
    override fun areItemsTheSame(oldItem: MessageDTO, newItem: MessageDTO): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MessageDTO, newItem: MessageDTO): Boolean {
        return oldItem.id == newItem.id
    }
}