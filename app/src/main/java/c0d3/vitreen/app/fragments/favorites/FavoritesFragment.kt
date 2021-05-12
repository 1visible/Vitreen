package c0d3.vitreen.app.fragments.favorites

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.dto.ProductDTO
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*

class FavoritesFragment : VFragment(
    layoutId = R.layout.fragment_favorites,
    topIcon = R.drawable.bigicon_favorites,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_favorites
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show loading spinner and hide empty view
        setSpinnerVisibility(VISIBLE)
        setEmptyView(GONE)

        // Listen to favorites
        showFavorites()

        // Get current user informations
        try {
            viewModel.getUser(user!!).observe(viewLifecycleOwner, { pair ->
                val exception = pair.first
                val user = pair.second
                // If the call fails, show error message, hide loading spinner and show empty view
                if(handleError(exception, R.string.no_products)) return@observe

                // Else, update products for listener
                viewModel.getProducts(limit = false, ids = user.favoritesIds, owner = viewLifecycleOwner)
            })
        } catch (_: NullPointerException) {
            // Update favorites with empty favorites if the user can't be found
            viewModel.productsLiveData.value = -1 to mutableListOf()
        }
    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun adapterOnClick(product: ProductDTO) {
        navigateTo(R.id.from_favorites_to_product, KEY_PRODUCT_ID to product.id)
    }

    private fun showFavorites() {
        // Observe products without making any request
        viewModel.productsLiveData.value = -1 to mutableListOf()
        viewModel.productsLiveData.observe(viewLifecycleOwner, { pair ->
            val exception = pair.first
            val products = pair.second
            // If the call fails, show error message, hide loading spinner and show empty view
            if(handleError(exception, R.string.no_favorites)) return@observe

            // Else if there is no products to display, hide loading spinner and show empty view
            if(products.isNullOrEmpty()) {
                setSpinnerVisibility(GONE)
                setEmptyView(VISIBLE, R.string.no_favorites)
                return@observe
            }

            // Else, show products in recycler view
            val adapter = ProductAdapter { product -> adapterOnClick(product) }
            adapter.submitList(products.map { product -> product.toDTO() })
            recyclerViewProducts.adapter = adapter
            recyclerViewProducts.visibility = VISIBLE
        })
    }
}