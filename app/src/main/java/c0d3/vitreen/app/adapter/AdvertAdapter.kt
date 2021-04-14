package c0d3.vitreen.app.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.R.string.euros
import c0d3.vitreen.app.models.mini.AdvertMini
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.advert_item.view.*

class AdvertAdapter(private val onClick: (AdvertMini) -> Unit) :
    ListAdapter<AdvertMini, AdvertAdapter.AdvertViewHolder>(AdvertDiffCallback) {


    class AdvertViewHolder(itemView: View, val onClick: (AdvertMini) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private var currentAdvert: AdvertMini? = null

        private val storage = Firebase.storage
        private val storageRef = storage.reference

        init {
            itemView.setOnClickListener {
                currentAdvert?.let {
                    onClick(it)
                }
            }
        }

        fun bind(advert: AdvertMini) {
            currentAdvert = advert
            val advertImageRef = storageRef.child("images/${currentAdvert!!.id}/image_0.png")
            /*val localFile = File.createTempFile("image", "jpg")
            advertImageRef.getFile(localFile)
                    .addOnSuccessListener {
                        if (currentAdvert != null) {
                            itemView.homeAdvertImageView.setImageURI()
                            itemView.homeAdvertTitle.text = currentAdvert!!.title
                            itemView.homeAdvertDescription.text = currentAdvert!!.description
                            itemView.homeAdvertPrice.text = currentAdvert!!.price.toString()
                        }
                    }*/
            val ONE_MEGABYTE: Long = 1024 * 1024
            advertImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                if (currentAdvert != null) {
                    itemView.homeAdvertImageView.setImageBitmap(
                        BitmapFactory.decodeByteArray(
                            it,
                            0,
                            it.size
                        )
                    )
                    itemView.homeAdvertTitle.text = currentAdvert!!.title
                    itemView.homeAdvertDescription.text = currentAdvert!!.description
                    itemView.homeAdvertPrice.text =
                        "${currentAdvert!!.price.toString()}${(itemView as Fragment).getString(euros)}"
                }
            }.addOnFailureListener {
                // Handle any errors
            }
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.advert_item, parent, false)
        return AdvertViewHolder(view, onClick)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: AdvertViewHolder, position: Int) {
        val advert = getItem(position)
        holder.bind(advert)

    }
}

object AdvertDiffCallback : DiffUtil.ItemCallback<AdvertMini>() {
    override fun areItemsTheSame(oldItem: AdvertMini, newItem: AdvertMini): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AdvertMini, newItem: AdvertMini): Boolean {
        return oldItem.id == newItem.id
    }
}