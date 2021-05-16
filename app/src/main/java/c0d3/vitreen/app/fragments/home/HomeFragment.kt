package c0d3.vitreen.app.fragments.home

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.*
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants.Companion.VTAG
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_register1.*
import kotlinx.android.synthetic.main.loading_spinner.*
import kotlinx.android.synthetic.main.search_section.*


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
        searchSection.visibility = GONE
        emptyView.visibility = GONE
        recyclerViewProducts.visibility = GONE

        viewModel.getProducts(limit = true).observe(viewLifecycleOwner, { (exception, products) ->
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

            (textInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)
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

            (textInputLocation.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        })

        // On search button click, make the query
        buttonSearch.setOnClickListener {
            // Check if required inputs are filled
            if(areAllInputsEmpty(textInputCategory, textInputLocation, editTextTitle, editTextPriceMin, editTextPriceMax)) {
                toggleSearchSectionVisibility()
                return@setOnClickListener
            }

            // Get all search filters
            val title = inputToString(editTextTitle)
            val priceMin = inputToString(editTextPriceMin)?.toDoubleOrNull()
            val priceMax = inputToString(editTextPriceMax)?.toDoubleOrNull()
            val location: Location? = viewModel.locations.value?.second?.firstOrNull { it.city == inputToString(textInputLocation) }
            val category: Category? = viewModel.categories.value?.second?.firstOrNull { it.name == inputToString(textInputCategory) }

            viewModel.getProducts(
                limit = false,
                title = title,
                priceMin = priceMin,
                priceMax = priceMax,
                location = location,
                category = category
            )

            toggleSearchSectionVisibility()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_products -> toggleSearchSectionVisibility()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleSearchSectionVisibility(): Boolean {
        if(searchSection.visibility == VISIBLE) {
            if(searchSection.focusedChild != null && context != null) {
                val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(searchSection.focusedChild.windowToken, 0)
            }

            searchSection.visibility = GONE
            editTextTitle.editText?.text?.clear()
            textInputCategory.editText?.text?.clear()
            textInputLocation.editText?.text?.clear()
            editTextPriceMin.editText?.text?.clear()
            editTextPriceMax.editText?.text?.clear()
        } else
            searchSection.visibility = VISIBLE

        return true
    }

    private fun adapterOnClick(product: Product) {
        viewModel.product = product
        navigateTo(R.id.from_home_to_product)
    }

}