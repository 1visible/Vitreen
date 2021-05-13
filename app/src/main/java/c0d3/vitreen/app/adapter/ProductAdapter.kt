package c0d3.vitreen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.dto.ProductDTO
import kotlinx.android.synthetic.main.product_item.view.*

class ProductAdapter(private val onClick: (ProductDTO) -> Unit)
    : ListAdapter<ProductDTO, ProductAdapter.ProductViewHolder>(ProductDiffCallback) {

    class ProductViewHolder(itemView: View, val onClick: (ProductDTO) -> Unit): RecyclerView.ViewHolder(itemView) {
        private var productDTO: ProductDTO = ProductDTO()

        init {
            itemView.setOnClickListener {
                onClick(productDTO)
            }
        }

        fun bind(product: ProductDTO) {
            productDTO = product

            // Display product image if available
            if(productDTO.image != null)
                itemView.imageViewProduct.setImageBitmap(productDTO.image)

            // Fill product with informations
            itemView.textViewTitle.text = productDTO.title
            itemView.textViewCategory.text = productDTO.category.name
            val zipCode = if(productDTO.location.zipCode == null) "?" else productDTO.location.zipCode.toString()
            itemView.textViewLocation.text = itemView.context.getString(R.string.location_template, productDTO.location.city, zipCode)
            itemView.textViewPrice.text = itemView.context.getString(R.string.price, productDTO.price)
        }

    }

    // Create and inflate view and return ProductViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(view, onClick)
    }

    // Get current product and use it to bind view.
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

}

object ProductDiffCallback : DiffUtil.ItemCallback<ProductDTO>() {
    override fun areItemsTheSame(oldItem: ProductDTO, newItem: ProductDTO): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ProductDTO, newItem: ProductDTO): Boolean {
        return oldItem.id == newItem.id
    }
}

