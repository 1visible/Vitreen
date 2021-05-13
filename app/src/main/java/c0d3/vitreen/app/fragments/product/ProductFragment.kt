package c0d3.vitreen.app.fragments.product

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.models.Consultation
import c0d3.vitreen.app.models.Discussion
import c0d3.vitreen.app.models.Message
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants.Companion.KEY_DISCUSSION_ID
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.recyclerViewProducts
import kotlinx.android.synthetic.main.product_item.view.*
import java.util.*

class ProductFragment : VFragment(
    layoutId = R.layout.fragment_product,
    topIcon = R.drawable.bigicon_adding,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_product
) {

    private var imageIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set elements visibility (while loading)
        // TODO

        viewModel.product.observe(viewLifecycleOwner, { product ->
            val city = viewModel.user.value?.second?.location?.city
            val consultation = Consultation(city = city)

            product.id?.let { id -> viewModel.addConsultation(id, consultation) }
            fillProductDetails(product)

            // Check if there are loaded images
            if(product.images.isNullOrEmpty())
                return@observe

            // Show previous and next buttons to switch between images
            buttonPreviousImage.visibility = VISIBLE
            buttonNextImage.visibility = VISIBLE

            // Display product images (first one)
            imageViewProduct.setImageBitmap(product.images[imageIndex])

            // On previous button click, go to previous image
            buttonPreviousImage.setOnClickListener {
                if(product.images.isEmpty())
                    return@setOnClickListener

                imageIndex = if (imageIndex <= 0) (product.images.size - 1) else imageIndex--
                imageViewProduct.setImageBitmap(product.images[imageIndex])
            }

            // On next button click, go to next image
            buttonNextImage.setOnClickListener {
                if(product.images.isEmpty())
                    return@setOnClickListener

                imageIndex = if (imageIndex >= product.images.size - 1) 0 else imageIndex++
                imageViewProduct.setImageBitmap(product.images[imageIndex])
            }

            /*
            buttonSendMessage.setOnClickListener { view ->
                    viewModel.getUser(user!!).observeOnce(viewLifecycleOwner, { pairUser ->
                        if (handleError(pairUser.first)) return@observeOnce
                        var firstMessage = ArrayList<Message>()
                        firstMessage.add(
                            Message(
                                pairUser.second.id,
                                getString(R.string.createDiscussion)
                            )
                        )
                        val discussion = Discussion(
                            pairUser.second.id,
                            pair.second.id,
                            pair.second.title,
                            pair.second.ownerId,
                            firstMessage
                        )
                        viewModel.addDiscussion(discussion)
                            .observeOnce(viewLifecycleOwner, addDiscussion@{ pair ->
                                if (handleError(pair.first)) return@addDiscussion
                                navigateTo(
                                    R.id.action_navigation_product_to_navigation_discussion,
                                    KEY_DISCUSSION_ID to pair.second
                                )
                            })
                    })
                }
             */
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_favorite -> setFavorite(true)
            R.id.action_remove_favorite -> setFavorite(false)
            R.id.action_statistics -> {
                if(viewModel.product.value != null)
                    navigateTo(R.id.from_product_to_statistics)
                else
                    showSnackbarMessage(R.string.NotFoundException)
                true
            }
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
        return true
    }

    /*
    private fun setFavorite(setFavorite: Boolean): Boolean {
        // Check if the user is signed in
        if(!isUserSignedIn()) {
            showMessage(R.string.network_error)
            return true
        }

        // Try to add product to favorites
        try {
            viewModel.getUser(user!!).observe(viewLifecycleOwner, { pair ->
                val exception = pair.first
                val user = pair.second
                // If the call fails, show error message and hide loading spinner
                if (handleError(exception)) return@observe

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
                        favoritesIds.remove(productId)
                    } else
                        return@observe

                    // Update user with new favorites
                    viewModel.updateUser(user.id!!, favoritesIds = favoritesIds).observeOnce(viewLifecycleOwner, { exception2 ->
                        // If the call fails, show error message and hide loading spinner
                        if (handleError(exception2)) {
                            if(setFavorite)
                                favoritesIds.remove(productId)
                            else
                                favoritesIds.add(productId!!)

                            return@observeOnce
                        }

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

     */

}