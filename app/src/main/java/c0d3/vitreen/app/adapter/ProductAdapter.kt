package c0d3.vitreen.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.dto.ProductDTO
import c0d3.vitreen.app.utils.FirestoreViewModel
import kotlinx.android.synthetic.main.product_item.view.*

class ProductAdapter(private val viewModel: FirestoreViewModel, private val owner: LifecycleOwner, private val onClick: (ProductDTO) -> Unit)
    : ListAdapter<ProductDTO, ProductAdapter.ProductViewHolder>(ProductDiffCallback) {

    class ProductViewHolder(private val viewModel: FirestoreViewModel, private val owner: LifecycleOwner, itemView: View, val onClick: (ProductDTO) -> Unit): RecyclerView.ViewHolder(itemView) {
        private var productDTO: ProductDTO? = null

        init {
            itemView.setOnClickListener {
                productDTO?.let { product -> onClick(product) }
            }
        }

        fun bind(product: ProductDTO) {
            productDTO = product

            // Fill product with informations
            viewModel.getImages(product.id, 1).observeOnce(owner, { pair ->
                val errorCode = pair.first
                val images = pair.second

                if(errorCode == -1 && images.isNotEmpty())
                    itemView.imageViewProduct.setImageBitmap(images.first())

                itemView.textViewTitle.text = product.title
                itemView.textViewCategory.text = product.category.name
                val zipCode = if(product.location.zipCode == null) "?" else product.location.zipCode.toString()
                itemView.textViewLocation.text = itemView.context.getString(R.string.location_template, product.location.city, zipCode)
                itemView.textViewPrice.text = itemView.context.getString(R.string.price, product.price)
            })
        }

    }

    // Create and inflate view and return ProductViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(viewModel, owner, view, onClick)
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

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner, object: Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}