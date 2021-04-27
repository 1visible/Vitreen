package c0d3.vitreen.app.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.dto.sdto.ProductSDTO
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.product_item.view.*

class ProductAdapter(private val onClick: (ProductSDTO) -> Unit) :
        ListAdapter<ProductSDTO, ProductAdapter.ProductViewHolder>(ProductDiffCallback) {


    class ProductViewHolder(itemView: View, val onClick: (ProductSDTO) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
        private var currentProduct: ProductSDTO? = null

        private val storage = Firebase.storage
        private val storageRef = storage.reference

        init {
            itemView.setOnClickListener {
                currentProduct?.let {
                    onClick(it)
                }
            }
        }

        fun bind(product: ProductSDTO) {
            currentProduct = product
            val productImageRef = storageRef.child("images/${currentProduct!!.id}/image_0")
            val ONE_MEGABYTE: Long = 1024 * 1024
            productImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                if (currentProduct != null) {
                    itemView.imageViewProduct.setImageBitmap(
                            BitmapFactory.decodeByteArray(
                                    it,
                                    0,
                                    it.size
                            )
                    )
                    itemView.textViewTitle.text = currentProduct!!.title
                    itemView.textViewCategory.text = currentProduct!!.category
                    itemView.textViewLocation.text = currentProduct!!.location
                    itemView.textViewPrice.text = "${currentProduct!!.price}€"
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

object ProductDiffCallback : DiffUtil.ItemCallback<ProductSDTO>() {
    override fun areItemsTheSame(oldItem: ProductSDTO, newItem: ProductSDTO): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ProductSDTO, newItem: ProductSDTO): Boolean {
        return oldItem.id == newItem.id
    }
}