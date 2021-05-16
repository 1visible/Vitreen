package c0d3.vitreen.app.fragments.product

import android.content.Context
import android.os.Bundle
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_adding2.*

class Modify2Fragment : VFragment(
    layoutId = R.layout.fragment_adding2,
    topIcon = R.drawable.bigicon_adding,
    requireAuth = true,
    loginNavigationId = R.id.from_adding1_to_login
) {
    private var product_title: String? = null
    private var product_price: String? = null
    private var product_location: String? = null
    private var product_category: Category? = null
    private var product_description: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        product_title = arguments?.getString(Constants.KEY_TITLE)
        product_price = arguments?.getString(Constants.KEY_PRICE)
        product_category = arguments?.get(Constants.KEY_CATEGORY) as? Category?
        product_location = arguments?.getString(Constants.KEY_LOCATION)
        product_description = arguments?.getString(Constants.KEY_DESCRIPTION)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!viewModel.isUserSignedIn) return
        if (product_title == null || product_price == null || product_location == null || product_category == null || product_category !is Category || product_description == null) {
            showSnackbarMessage(R.string.error_placeholder)
            goBack()
            return
        }
        if (viewModel.product.brand != null) editTextBrand.editText?.setText(viewModel.product.brand)
        if (viewModel.product.size != null) editTextDimensions.editText?.setText(viewModel.product.size)
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
            val product_brand = inputToString(editTextBrand)
            val product_dimensions = inputToString(editTextDimensions)
            if (viewModel.product.title != product_title) viewModel.product.title = product_title!!
            if (!viewModel.product.category.equals(product_category)) viewModel.product.category =
                product_category!!
            if (viewModel.product.price != product_price!!.toDouble()) viewModel.product.price =
                product_price!!.toDouble()
            if (viewModel.product.description != product_description) viewModel.product.description =
                product_description!!
            if (viewModel.product.location.city != product_location) {
                viewModel.getLocation(product_location!!)
                    .observeOnce(viewLifecycleOwner, { (exception, location) ->
                        // If the call fails, show error message
                        if (exception != -1 && exception != R.string.NotFoundException) {
                            showSnackbarMessage(exception)
                            return@observeOnce
                        }

                        // Else if location could not be found, create new location
                        if (exception == R.string.NotFoundException) {
                            location.city = product_location!!
                            location.zipCode = null
                            viewModel.addLocation(location)
                        }
                        viewModel.product.location = location
                    })
            }
            if (viewModel.product.brand != product_brand) viewModel.product.brand =
                product_brand
            if (viewModel.product.size != product_dimensions) viewModel.product.size =
                product_dimensions
            if (isProductChanged(product, viewModel.product)) {
                viewModel.updateProduct(viewModel.product)
            }
            navigateTo(R.id.from_modify2_to_product)
        }
    }

    private fun isProductChanged(oldProduct: Product, newProduct: Product): Boolean {
        return ((oldProduct.title != newProduct.title) || (!oldProduct.category.equals(newProduct.category)) || (oldProduct.price != newProduct.price) || (oldProduct.description != newProduct.description) || (!oldProduct.location.equals(
            newProduct.location
        )) || (oldProduct.brand != newProduct.brand) || (oldProduct.size != newProduct.size))
    }
}