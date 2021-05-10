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
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*

class HomeFragment : VFragment(
    layoutId = R.layout.fragment_home,
    topIcon = R.drawable.bigicon_logo,
    topTitleId = R.string.welcome,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_home
) {

    // TODO : A VERIFIER !!!!!!!

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
                // Else show the products
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
            viewModel.getUser(user!!).observeOnce(viewLifecycleOwner, { pair ->
                val errorCode = pair.first
                val user = pair.second
                // If the call fails, show error message, hide loading spinner and show empty view
                if(handleError(errorCode, R.string.no_products)) return@observeOnce

                // Else, show the products according to user location
                showProducts(user.location)
            })
        }

        // Fill categories in the search section
        viewModel.getCategories().observeOnce(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val categories = pair.second
            // If the call fails, show error message, hide loading spinner and show empty view
            if(errorCode == -1) return@observeOnce

            // Else, set categories as editTextCategories choices if possible
            if(context != null) {
                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, categories.map { it.name })
                (textInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)
            }
        })

        // Fill locations in the search section
        viewModel.getLocations().observeOnce(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val locations = pair.second
            // If the call fails, show error message, hide loading spinner and show empty view
            if(errorCode == -1) return@observeOnce

            // Else, set locations as editTextLocations choices if possible
            if(context != null) {
                val locationsList = ArrayList(locations.map { it.name })
                locationsList.add(0, getString(R.string.my_location))

                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, locationsList)
                (autoCompleteLocation.editText as? AutoCompleteTextView)?.setAdapter(adapter)
            }
        })

        // On search button click, query products according to search filters
        buttonResearch.setOnClickListener {
            if(areAllInputsEmpty(editTextResearchText, editTextMaxPrice, textInputCategory, autoCompleteLocation, editTextBrand))
                return@setOnClickListener // TODO : Ajouter un message snackbar

            val title = inputToString(editTextResearchText)
            val price = inputToString(editTextMaxPrice)?.toDoubleOrNull()
            val brand = inputToString(editTextBrand)
            val location: Location? = viewModel.locationsLiveData.value?.second?.findLast { it.name == inputToString(autoCompleteLocation) }
            val category: Category? = viewModel.categoriesLiveData.value?.second?.findLast { it.name == inputToString(textInputCategory) }

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

    /* Opens Product when RecyclerView item is clicked. */
    private fun adapterOnClick(product: ProductDTO) { // TODO : Déplacement vers fragment annonce
        navigateTo(R.id.action_navigation_home_to_navigation_product, KEY_PRODUCT_ID to product.id)
    }

    private fun showProducts(location: Location? = null) {
        viewModel.getProducts(location = location).observe(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val products = pair.second
            // If the call fails, show error message, hide loading spinner and show empty view
            if(handleError(errorCode, R.string.no_products)) return@observe

            // Else if there is no products to display, hide loading spinner and show empty view
            if(products.isEmpty()) {
                setSpinnerVisibility(GONE)
                setEmptyView(VISIBLE, R.string.no_products)
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