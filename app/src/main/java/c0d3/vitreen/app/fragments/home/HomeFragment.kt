package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_register1.*
import kotlinx.android.synthetic.main.loading_spinner.*


class HomeFragment : VFragment(
    layoutId = R.layout.fragment_home,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_home
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set elements visibility (while loading)
        emptyView.visibility = GONE
        recyclerViewProducts.visibility = GONE

        viewModel.getProducts(viewModel.searchQuery).observe(viewLifecycleOwner, { (exception, products) ->
            // When the call finishes, hide loading spinner
            loadingSpinner.visibility = GONE

            // If the call failed: show error message and show empty view
            if(exception != -1) {
                showSnackbarMessage(exception)
                recyclerViewProducts.visibility = GONE
                emptyView.visibility = VISIBLE
                return@observe
            }

            // If there are no products: show empty view
            if(products.isNullOrEmpty()) {
                recyclerViewProducts.visibility = GONE
                emptyView.visibility = VISIBLE
                return@observe
            }

            // Else, display products in the recycler view
            val adapter = ProductAdapter { product -> adapterOnClick(product) }
            adapter.submitList(products)
            recyclerViewProducts.adapter = adapter
            emptyView.visibility = GONE
            recyclerViewProducts.visibility = VISIBLE
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_products -> { navigateTo(R.id.from_home_to_search); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun adapterOnClick(product: Product) {
        viewModel.product = product
        navigateTo(R.id.from_home_to_product)
    }

}