package c0d3.vitreen.app.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.mini.ProductMini
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.product_item.view.*

class ProductAdapter(private val onClick: (ProductMini) -> Unit) :
        ListAdapter<ProductMini, ProductAdapter.ProductViewHolder>(ProductDiffCallback) {


    class ProductViewHolder(itemView: View, val onClick: (ProductMini) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
        private var currentProduct: ProductMini? = null

        private val storage = Firebase.storage
        private val storageRef = storage.reference

        init {
            itemView.setOnClickListener {
                currentProduct?.let {
                    onClick(it)
                }
            }
        }

        fun bind(product: ProductMini) {
            currentProduct = product
            val productImageRef = storageRef.child("images/${currentProduct!!.id}/image_0.png")
            /*val localFile = File.createTempFile("image", "jpg")
            productImageRef.getFile(localFile)
                    .addOnSuccessListener {
                        if (currentProduct != null) {
                            itemView.homeProductImageView.setImageURI()
                            itemView.homeProductTitle.text = currentProduct!!.title
                            itemView.homeProductDescription.text = currentProduct!!.description
                            itemView.homeProductPrice.text = currentProduct!!.price.toString()
                        }
                    }*/
            val ONE_MEGABYTE: Long = 1024 * 1024
            productImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                if (currentProduct != null) {
                    itemView.homeProductImageView.setImageBitmap(
                            BitmapFactory.decodeByteArray(
                                    it,
                                    0,
                                    it.size
                            )
                    )
                    itemView.homeProductTitle.text = currentProduct!!.title
                    itemView.homeProductDescription.text = currentProduct!!.description
                    itemView.homeProductPrice.text =
                            "${currentProduct!!.price.toString()}€"
                }
            }.addOnFailureListener {
                // Handle any errors
            }
        }
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(view, onClick)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)

    }
}

object ProductDiffCallback : DiffUtil.ItemCallback<ProductMini>() {
    override fun areItemsTheSame(oldItem: ProductMini, newItem: ProductMini): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ProductMini, newItem: ProductMini): Boolean {
        return oldItem.id == newItem.id
    }
}