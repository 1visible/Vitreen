package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import android.view.View
import android.widget.*
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.utils.SearchQuery
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_register1.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.loading_spinner.*


class SearchFragment : VFragment(
    layoutId = R.layout.fragment_search
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            if(areAllInputsEmpty(
                    textInputCategory,
                    textInputLocation,
                    editTextTitle,
                    editTextBrand,
                    editTextPriceMin,
                    editTextPriceMax
            )) {
                viewModel.searchQuery = SearchQuery()
                goBack()
                return@setOnClickListener
            }

            // Get all search filters
            val title = inputToString(editTextTitle)
            val brand = inputToString(editTextBrand)
            val priceMin = inputToString(editTextPriceMin)?.toDoubleOrNull()
            val priceMax = inputToString(editTextPriceMax)?.toDoubleOrNull()
            val location: Location? = viewModel.locations.value?.second?.firstOrNull { it.city == inputToString(textInputLocation) }
            val category: Category? = viewModel.categories.value?.second?.firstOrNull { it.name == inputToString(textInputCategory) }

            viewModel.searchQuery = SearchQuery(
                title = title,
                brand = brand,
                priceMin = priceMin,
                priceMax = priceMax,
                location = location,
                category = category
            )

            goBack()
        }
    }

}