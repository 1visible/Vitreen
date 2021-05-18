package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.SearchQuery
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.loading_spinner.*

class FavoritesFragment : VFragment(
    layoutId = R.layout.fragment_favorites
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set elements visibility (while loading)
        emptyView.visibility = GONE
        recyclerViewProducts.visibility = GONE

        if(!viewModel.isUserSignedIn) {
            // Set elements visibility
            loadingSpinner.visibility = GONE
            emptyView.visibility = VISIBLE
            recyclerViewProducts.visibility = GONE
            // Show error message
            showSnackbarMessage(R.string.SignedOutException)
            return
        }

        viewModel.user.observe(viewLifecycleOwner, { (exception, user) ->
            // If the call failed: show error message and show empty view
            if(exception != -1) {
                showSnackbarMessage(exception)
                loadingSpinner.visibility = GONE
                recyclerViewProducts.visibility = GONE
                emptyView.visibility = VISIBLE
                return@observe
            }

            // If the user has no favorites, show empty view
            if(user.favoritesIds.isEmpty()) {
                loadingSpinner.visibility = GONE
                recyclerViewProducts.visibility = GONE
                emptyView.visibility = VISIBLE
                return@observe
            }

            // Else, get favorites list
            recyclerViewProducts.visibility = GONE
            emptyView.visibility = GONE
            loadingSpinner.visibility = VISIBLE

            viewModel.getProducts(SearchQuery(ids = user.favoritesIds)).observe(viewLifecycleOwner, observe1@ { (exception, products) ->
                // When the call finishes, hide loading spinner
                loadingSpinner.visibility = GONE

                // If the call failed: show error message and show empty view
                if(exception != -1) {
                    showSnackbarMessage(exception)
                    recyclerViewProducts.visibility = GONE
                    emptyView.visibility = VISIBLE
                    return@observe1
                }

                // If there are no products: show empty view
                if(products.isEmpty()) {
                    recyclerViewProducts.visibility = GONE
                    emptyView.visibility = VISIBLE
                    return@observe1
                }

                // Else, display products in the recycler view
                val adapter = ProductAdapter { product -> adapterOnClick(product) }
                adapter.submitList(products)
                recyclerViewProducts.adapter = adapter
                emptyView.visibility = GONE
                recyclerViewProducts.visibility = VISIBLE
            })
        })
    }

    private fun adapterOnClick(product: Product) {
        viewModel.product = product
        navigateTo(R.id.from_favorites_to_product)
    }
}