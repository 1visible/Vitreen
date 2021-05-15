package c0d3.vitreen.app.fragments.favorites

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants.Companion.VTAG
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.loading_spinner.*

class FavoritesFragment : VFragment(
    layoutId = R.layout.fragment_favorites,
    topIcon = R.drawable.bigicon_favorites
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(viewModel.isUserSignedIn) {
            // Set elements visibility (while loading)
            emptyView.visibility = GONE
            recyclerViewProducts.visibility = GONE
        } else {
            // Set elements visibility
            loadingSpinner.visibility = GONE
            emptyView.visibility = VISIBLE
            recyclerViewProducts.visibility = GONE
            // Show error message
            showSnackbarMessage(R.string.SignedOutException)
        }

        viewModel.user.observe(viewLifecycleOwner, { (exception, user) ->
            Log.i(VTAG, "User observed")
            // If the call failed: show error message and show empty view
            if(exception != -1) {
                Log.i(VTAG, "Error")
                showSnackbarMessage(exception)
                loadingSpinner.visibility = GONE
                recyclerViewProducts.visibility = GONE
                emptyView.visibility = VISIBLE
                return@observe
            }

            // If the user has no favorites, show empty view
            if(user.favoritesIds.isEmpty()) {
                Log.i(VTAG, "No favorites")
                loadingSpinner.visibility = GONE
                recyclerViewProducts.visibility = GONE
                emptyView.visibility = VISIBLE
                return@observe
            }

            Log.i(VTAG, "Put favorites : " + user.favoritesIds)
            // Else, get favorites list
            recyclerViewProducts.visibility = GONE
            emptyView.visibility = GONE
            loadingSpinner.visibility = VISIBLE
            viewModel.getProducts(limit = false, ids = user.favoritesIds)

            viewModel.products.observe(viewLifecycleOwner, observe1@ { (exception, products) ->
                Log.i(VTAG, "Products observed")
                // When the call finishes, hide loading spinner
                loadingSpinner.visibility = GONE

                // If the call failed: show error message and show empty view
                if(exception != -1) {
                    Log.i(VTAG, "Products failed")
                    showSnackbarMessage(exception)
                    recyclerViewProducts.visibility = GONE
                    emptyView.visibility = VISIBLE
                    return@observe1
                }

                // If there are no products: show empty view
                if(products.isNullOrEmpty()) {
                    Log.i(VTAG, "No products")
                    recyclerViewProducts.visibility = GONE
                    emptyView.visibility = VISIBLE
                    return@observe1
                }

                Log.i(VTAG, "Put products")
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
        viewModel.select(product)
        navigateTo(R.id.from_favorites_to_product)
    }
}