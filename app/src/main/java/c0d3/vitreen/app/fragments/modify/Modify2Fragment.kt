package c0d3.vitreen.app.fragments.modify

import android.content.Context
import android.os.Bundle
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_modify2.*

class Modify2Fragment : VFragment(
    layoutId = R.layout.fragment_modify2,
    requireAuth = true,
    loginNavigationId = R.id.from_modify2_to_login
) {
    private var productTitle: String? = null
    private var productPrice: String? = null
    private var productLocation: String? = null
    private var productCategory: Category? = null
    private var productDescription: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        productTitle = arguments?.getString(Constants.KEY_TITLE)
        productPrice = arguments?.getString(Constants.KEY_PRICE)
        productCategory = arguments?.get(Constants.KEY_CATEGORY) as? Category?
        productLocation = arguments?.getString(Constants.KEY_LOCATION)
        productDescription = arguments?.getString(Constants.KEY_DESCRIPTION)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isUserSignedIn)
            return

        if (productTitle == null
            || productPrice == null
            || productLocation == null
            || productCategory == null
            || productCategory !is Category
            || productDescription == null) {
            showSnackbarMessage(R.string.error_placeholder)
            goBack()
            return
        }

        if (viewModel.product.brand != null)
            editTextBrand.editText?.setText(viewModel.product.brand)

        if (viewModel.product.size != null)
            editTextDimensions.editText?.setText(viewModel.product.size)

        relativeLayoutProduct.visibility = View.GONE
        buttonAddImage.visibility = View.GONE

        buttonConfirmation.setOnClickListener {
            val product = Product(
                viewModel.product.title,
                viewModel.product.description,
                viewModel.product.price,
                viewModel.product.brand,
                viewModel.product.size,
                viewModel.product.consultations,
                viewModel.product.reporters,
                viewModel.product.location,
                viewModel.product.category,
                viewModel.product.imagesPaths,
                viewModel.product.ownerId,
                viewModel.product.modifiedAt
            )

            val productBrand = inputToString(editTextBrand)
            val productDimensions = inputToString(editTextDimensions)

            if (viewModel.product.title != productTitle)
                viewModel.product.title = productTitle!!

            if (viewModel.product.category != productCategory)
                viewModel.product.category = productCategory!!

            if (viewModel.product.price != productPrice!!.toDouble())
                viewModel.product.price = productPrice!!.toDouble()

            if (viewModel.product.description != productDescription)
                viewModel.product.description = productDescription!!

            if (viewModel.product.location.city != productLocation) {
                viewModel.getLocation(productLocation!!).observeOnce(viewLifecycleOwner, { (exception, location) ->
                    // If the call fails, show error message
                    if (exception != -1 && exception != R.string.NotFoundException) {
                        showSnackbarMessage(exception)
                        return@observeOnce
                    }

                    // Else if location could not be found, create new location
                    if (exception == R.string.NotFoundException) {
                        location.city = productLocation!!
                        location.zipCode = null
                        viewModel.addLocation(location)
                    }

                    viewModel.product.location = location
                })
            }

            if (viewModel.product.brand != productBrand)
                viewModel.product.brand = productBrand

            if (viewModel.product.size != productDimensions)
                viewModel.product.size = productDimensions

            if (isProductChanged(product, viewModel.product))
                viewModel.updateProduct(viewModel.product)

            navigateTo(R.id.from_modify2_to_product)
        }
    }

    private fun isProductChanged(oldProduct: Product, newProduct: Product): Boolean {
        return (oldProduct.title != newProduct.title
                || oldProduct.category != newProduct.category
                || oldProduct.price != newProduct.price
                || oldProduct.description != newProduct.description
                || oldProduct.location != newProduct.location
                || oldProduct.brand != newProduct.brand
                || oldProduct.size != newProduct.size)
    }
}