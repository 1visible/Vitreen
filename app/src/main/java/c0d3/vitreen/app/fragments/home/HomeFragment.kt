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
import kotlinx.android.synthetic.main.fragment_adding1.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.loading_spinner.*

class HomeFragment : VFragment(
    layoutId = R.layout.fragment_home,
    topIcon = R.drawable.bigicon_logo,
    topTitleId = R.string.welcome,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_home
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set elements visibility (while loading)
        emptyView.visibility = GONE
        recyclerViewProducts.visibility = GONE

        viewModel.products.observe(viewLifecycleOwner, { (exception, products) ->
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

        viewModel.user.observe(viewLifecycleOwner, { (exception, user) ->
            // TODO : Gérer le fait qu'il n'y ait pas de produits sur la location
            if (exception == -1)
                viewModel.getProducts(limit = false, location = user.location)
        })

        // Fill categories in the search section
        viewModel.categories.observe(viewLifecycleOwner, { (exception, categories) ->
            // If the call failed: show error message
            if(exception != -1) {
                showSnackbarMessage(exception)
                return@observe
            }

            // Else, put categories as edit text choices
            val categoryNames = categories.map { category -> category.name }
            val adapter = context?.let { context -> ArrayAdapter(context, R.layout.dropdown_menu_item, categoryNames) }

            // TODO (textInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        })

        // Fill locations in the search section
        viewModel.locations.observe(viewLifecycleOwner, { (exception, locations) ->
            // If the call failed: show error message
            if(exception != -1) {
                showSnackbarMessage(exception)
                return@observe
            }

            // Else, put locations as edit text choices
            val locationNames = locations.map { location -> location.city }
            val adapter = context?.let { context -> ArrayAdapter(context, R.layout.dropdown_menu_item, locationNames) }

            // TODO (autoCompleteLocation.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        })

        /*
        // On search button click, query products according to search filters
        buttonResearch.setOnClickListener {
            // Check if all inputs are empty
            if(areAllInputsEmpty(editTextMaxPrice, textInputCategory, autoCompleteLocation, editTextBrand, editTextResearchText))
                return@setOnClickListener

            // Get all search filters
            val title = inputToString(editTextResearchText)
            val price = inputToString(editTextMaxPrice)?.toDoubleOrNull()
            val brand = inputToString(editTextBrand)
            val location: Location? = viewModel.locationsLiveData.value?.second?.findLast { it.city == inputToString(autoCompleteLocation) }
            val category: Category? = viewModel.categoriesLiveData.value?.second?.findLast { it.name == inputToString(textInputCategory) }

            // Search products according to filters
            viewModel.getProducts(
                limit = false,
                title = title,
                price = price,
                brand = brand,
                location = location,
                category = category,
                owner = viewLifecycleOwner
            )
        }
        */
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_products -> {
                TODO()
                // navigate to search screen
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun adapterOnClick(product: Product) {
        viewModel.select(product)
        navigateTo(R.id.from_home_to_product)
    }

}