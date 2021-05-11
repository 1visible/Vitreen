package c0d3.vitreen.app.fragments.product

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.product_item.view.*
import java.lang.NullPointerException
import java.util.*

class ProductFragment : VFragment(
    layoutId = R.layout.fragment_product,
    topIcon = R.drawable.bigicon_adding,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_product,
    requireAuth = true,
    loginNavigationId = R.id.from_product_to_login
) {

    private var productId: String? = null
    private var imageIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)

        productId = arguments?.getString(KEY_PRODUCT_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If user is not signed in, skip this part
        if (!isUserSignedIn())
            return

        // Show loading spinner and hide empty view
        setSpinnerVisibility(VISIBLE)
        setEmptyView(GONE)

        // Try to show product if possible, otherwise go back to home
        try {
            viewModel.getProduct(productId!!).observe(viewLifecycleOwner, { pair ->
                val errorCode = pair.first
                val product = pair.second
                // If the call fails, show error message, hide loading spinner and go back to home
                if (handleError(errorCode)) {
                    navigateTo(R.id.from_product_to_home)
                    return@observe
                }

                // Else, get product images
                viewModel.getImages(product.id, product.nbImages).observe(viewLifecycleOwner, observe2@ { pair2 ->
                    val errorCode2 = pair2.first
                    val images = pair2.second
                    // If the call fails, show error message, hide loading spinner and go back to home
                    if (handleError(errorCode2)) {
                        navigateTo(R.id.from_product_to_home)
                        return@observe2
                    }

                    // Else, fill and display product informations
                    fillProductDetails(product)

                    // Check if there are loaded images
                    if(images.isNullOrEmpty())
                        return@observe2

                    // Show previous and next buttons to switch between images
                    buttonPreviousImage.visibility = VISIBLE
                    buttonNextImage.visibility = VISIBLE

                    // Display product images (first one)
                    imageViewProduct.setImageBitmap(images[imageIndex])

                    // On previous button click, go to previous image
                    buttonPreviousImage.setOnClickListener {
                        if(images.isEmpty())
                            return@setOnClickListener

                        imageIndex = if (imageIndex <= 0) (images.size - 1) else imageIndex--
                        imageViewProduct.setImageBitmap(images[imageIndex])
                    }

                    // On next button click, go to next image
                    buttonNextImage.setOnClickListener {
                        if(images.isEmpty())
                            return@setOnClickListener

                        imageIndex = if (imageIndex >= images.size - 1) 0 else imageIndex++
                        imageViewProduct.setImageBitmap(images[imageIndex])
                    }
                })
            })
        } catch (_: NullPointerException) {
            showMessage(R.string.error_404)
            navigateTo(R.id.from_product_to_home)
            return
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_favorite -> setFavorite(true)
            R.id.action_remove_favorite -> setFavorite(false)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fillProductDetails(product: Product) {
        // Fill product informations
        textViewTitle.text = product.title
        textViewDescription.text = product.description
        textViewPrice.text = getString(R.string.price, product.price)
        textViewCategory.text = product.category.name
        val zipCode = if(product.location.zipCode == null) "?" else product.location.zipCode.toString()
        textViewLocation.text = getString(R.string.location_template, product.location.city, zipCode)

        // Show product informations
        imageViewProduct.visibility = VISIBLE
        textViewTitle.visibility = VISIBLE
        textViewDescription.visibility = VISIBLE
        textViewPrice.visibility = VISIBLE
        textViewCategory.visibility = VISIBLE
        textViewLocation.visibility = VISIBLE

        // Show optional fields (if they exist)
        if(product.brand != null) {
            textViewBrand.text = product.brand
            textViewBrand.visibility = VISIBLE
        }

        if(product.size != null) {
            textViewDimensions.text = product.size
            textViewDimensions.visibility = VISIBLE
        }
    }

    private fun setFavorite(setFavorite: Boolean): Boolean {
        // Check if the user is signed in
        if(!isUserSignedIn()) {
            showMessage(R.string.network_error)
            return true
        }

        // Try to add product to favorites
        try {
            viewModel.getUser(user!!).observe(viewLifecycleOwner, { pair ->
                val errorCode = pair.first
                val user = pair.second
                // If the call fails, show error message and hide loading spinner
                if (handleError(errorCode)) return@observe

                // Check if product id could be retrieved
                if(productId == null) {
                    showMessage()
                    return@observe
                }

                val favoritesIds = user.favoritesIds

                // Update favorites with product id
                try {
                    if (setFavorite && !favoritesIds.contains(productId)) {
                        favoritesIds.add(productId!!)
                    } else if (!setFavorite && favoritesIds.contains(productId)) {
                        favoritesIds.remove(productId!!)
                    } else
                        return@observe

                    // Update user with new favorites
                    viewModel.updateUser(user.id, favoritesIds = favoritesIds).observeOnce(viewLifecycleOwner, { errorCode2 ->
                        // If the call fails, show error message and hide loading spinner
                        if (handleError(errorCode2)) return@observeOnce

                        // Else, toggle favorite icon visibility
                        setIconVisibility(R.id.action_add_favorite, !setFavorite)
                        setIconVisibility(R.id.action_remove_favorite, setFavorite)
                    })
                } catch(_: NullPointerException) {
                    showMessage()
                }
            })
        } catch(_: NullPointerException) {
            showMessage()
        }

        return true
    }

}