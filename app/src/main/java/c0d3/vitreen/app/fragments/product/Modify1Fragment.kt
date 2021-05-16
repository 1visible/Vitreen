package c0d3.vitreen.app.fragments.product

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_adding1.*
import java.util.*

class Modify1Fragment : VFragment(
    layoutId = R.layout.fragment_adding1,
    topIcon = R.drawable.bigicon_adding,
    requireAuth = true,
    loginNavigationId = R.id.from_adding1_to_login
) {
    private var categories: List<Category>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get categories to put them as edit text choices
        viewModel.categories.observe(viewLifecycleOwner, { (exception, categories) ->
            // If the call fails, show error message
            if (exception != -1) {
                showSnackbarMessage(exception)
                return@observe
            }

            // Else, put categories as edit text choices
            this.categories = categories
            val categoryNames = categories.map { category -> category.name }
            val adapter = context?.let { context ->
                ArrayAdapter(
                    context,
                    R.layout.dropdown_menu_item,
                    categoryNames
                )
            }

            (textInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        })
        editTextTitle.editText?.setText(viewModel.product.title)
        editTextDescription.editText?.setText(viewModel.product.description)
        editTextPrice.editText?.setText(viewModel.product.price.toString())
        editTextLocation.editText?.setText(viewModel.product.location.city)
        textInputCategory.editText?.setText(viewModel.product.category.name)
        buttonToAdding2.setOnClickListener {
            var currentCategory: Category? = null
            if (isAnyRequiredInputEmpty(
                    textInputCategory,
                    editTextTitle,
                    editTextPrice,
                    editTextLocation,
                    editTextDescription
                )
            )
                return@setOnClickListener
            val locationName =
                inputToString(editTextLocation)?.toLowerCase(Locale.ROOT)?.capitalize(Locale.ROOT)
            // Double check category and location after conversion
            if (locationName == null) {
                showSnackbarMessage(R.string.error_placeholder)
                return@setOnClickListener
            }
            categories?.forEach { category ->
                if (category.name == inputToString(textInputCategory)) currentCategory = category
            }
            navigateTo(
                R.id.from_modify1_to_modify2,
                Constants.KEY_TITLE to inputToString(editTextTitle),
                Constants.KEY_CATEGORY to currentCategory,
                Constants.KEY_DESCRIPTION to inputToString(editTextDescription),
                Constants.KEY_LOCATION to locationName,
                Constants.KEY_PRICE to inputToString(editTextPrice)
            )
        }
    }
}