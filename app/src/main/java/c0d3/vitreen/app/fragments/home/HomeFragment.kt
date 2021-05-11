package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.dto.ProductDTO
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_adding1.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_home.textInputCategory
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.NullPointerException

class HomeFragment : VFragment(
    layoutId = R.layout.fragment_home,
    topIcon = R.drawable.bigicon_logo,
    topTitleId = R.string.welcome,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_home
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show loading spinner and hide empty view
        setSpinnerVisibility(VISIBLE)
        setEmptyView(GONE)

        // If the user is signed out
        if(user == null) {
            // Try to sign in with anonymous account
            viewModel.signInAnonymously().observeOnce(viewLifecycleOwner, { errorCode ->
                // If the call fails, show error message, hide loading spinner and show empty view
                if(handleError(errorCode, R.string.no_products)) return@observeOnce

                // Else, show the products
                showProducts()
            })
        }
        // Else if the user is signed in anonymously
        else if(!isUserSignedIn()) {
            showProducts()
        }
        // Else (the user is signed in)
        else {
            // Get current user informations
            try {
                viewModel.getUser(user!!).observeOnce(viewLifecycleOwner, { pair ->
                    val errorCode = pair.first
                    val user = pair.second
                    // If the call fails, show error message, hide loading spinner and show empty view
                    if(handleError(errorCode, R.string.no_products)) return@observeOnce

                    // Else, show the products according to user location
                    showProducts(user.location)
                })
            } catch (_: NullPointerException) {
                showMessage()
            }
        }

        // Fill categories in the search section
        viewModel.getCategories().observeOnce(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val categories = pair.second
            // If the call fails, show error message and hide loading spinner
            if (handleError(errorCode)) return@observeOnce

            // Else, put categories as edit text choices
            val categoryNames = categories.map { category -> category.name }
            val adapter = context?.let { context -> ArrayAdapter(context, R.layout.dropdown_menu_item, categoryNames) }

            (textInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        })

        // Fill locations in the search section
        viewModel.getLocations().observeOnce(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val locations = pair.second
            // If the call fails, show error message and hide loading spinner
            if (handleError(errorCode)) return@observeOnce

            // Else, put locations as edit text choices
            val locationNames = locations.map { location -> location.city }
            val adapter = context?.let { context -> ArrayAdapter(context, R.layout.dropdown_menu_item, locationNames) }

            (autoCompleteLocation.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        })

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
            viewModel.getProducts(false, title, price, brand, location, category)
        }
    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // navigate to search screen
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun adapterOnClick(product: ProductDTO) {
        navigateTo(R.id.from_home_to_product, KEY_PRODUCT_ID to product.id)
    }

    private fun showProducts(location: Location? = null) {
        viewModel.getProducts(location = location).observe(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val products = pair.second
            // If the call fails, show error message, hide loading spinner and show empty view
            if(handleError(errorCode, R.string.no_products)) return@observe

            // Else if there is no products to display, hide loading spinner and show empty view
            if(products.isNullOrEmpty()) {
                setSpinnerVisibility(GONE)
                setEmptyView(VISIBLE, R.string.no_products)
                return@observe
            }

            // Else, show products in recycler view
            val adapter = ProductAdapter(viewModel, viewLifecycleOwner) { product -> adapterOnClick(product) }
            adapter.submitList(products.map { product -> product.toDTO() })
            recyclerViewProducts.adapter = adapter
            recyclerViewProducts.visibility = VISIBLE
        })
    }

}