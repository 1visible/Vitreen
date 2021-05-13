package c0d3.vitreen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Product
import kotlinx.android.synthetic.main.product_item.view.*

class ProductAdapter(private val onClick: (Product) -> Unit)
    : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback) {

    class ProductViewHolder(itemView: View, val onClick: (Product) -> Unit): RecyclerView.ViewHolder(itemView) {
        private var product: Product = Product()

        init {
            itemView.setOnClickListener {
                onClick(product)
            }
        }

        fun bind(product: Product) {
            this.product = product

            // Display product image if available
            if(product.images.isNotEmpty())
                itemView.imageViewProduct.setImageBitmap(product.images.first())

            // Fill product with informations
            itemView.textViewTitle.text = product.title
            itemView.textViewCategory.text = product.category.name
            val zipCode = if(product.location.zipCode == null) "?" else product.location.zipCode.toString()
            itemView.textViewLocation.text = itemView.context.getString(R.string.location_template, product.location.city, zipCode)
            itemView.textViewPrice.text = itemView.context.getString(R.string.price, product.price)
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
        holder.bind(getItem(position))
    }

}

object ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }
}

