package c0d3.vitreen.app.fragments.product

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.models.*
import c0d3.vitreen.app.utils.Constants.Companion.KEY_DISCUSSION_ID
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.loading_spinner.*
import kotlinx.android.synthetic.main.product_item.view.*
import java.util.*

class ProductFragment : VFragment(
    layoutId = R.layout.fragment_product,
    topIcon = R.drawable.bigicon_adding,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_product
) {

    private var product: Product? = null
    private var user: User? = null
    private var discussions: List<Discussion>? = null
    private var imageIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set elements visibility (while loading)
        // TODO
        setMenuItemVisibile(R.id.action_add_favorite, false)
        setMenuItemVisibile(R.id.action_remove_favorite, false)
        setMenuItemVisibile(R.id.action_send_message, false)
        setMenuItemVisibile(R.id.action_statistics, false)

        product = viewModel.product.value

        try {
            viewModel.user.observe(viewLifecycleOwner, { (exception, user) ->
                if(exception == -1) {
                    this.user = user

                    setFavoriteItemVisibility()
                    setMenuItemVisibile(R.id.action_send_message, true)

                    if(user.id == product!!.ownerId)
                        setMenuItemVisibile(R.id.action_statistics, true)
                    else
                        setMenuItemVisibile(R.id.action_statistics, false)
                } else {
                    this.user = null
                    setMenuItemVisibile(R.id.action_add_favorite, false)
                    setMenuItemVisibile(R.id.action_remove_favorite, false)
                    setMenuItemVisibile(R.id.action_send_message, false)
                    setMenuItemVisibile(R.id.action_statistics, false)
                }
            })

            viewModel.discussions.observe(viewLifecycleOwner, { (exception, discussions) ->
                this.discussions = if (exception == -1) discussions else null
            })

            viewModel.getProductImages(product!!).observeOnce(viewLifecycleOwner, { (exception, product) ->
                // When the call finishes, hide loading spinner
                loadingSpinner.visibility = GONE

                // If the call failed: show error message
                if(exception != -1)
                    showSnackbarMessage(exception)

                val city = viewModel.user.value?.second?.location?.city
                val consultation = Consultation(city = city)

                product.id?.let { id -> viewModel.addConsultation(id, consultation) }
                fillProductDetails(product)

                // Check if there are loaded images
                if(product.images.isNullOrEmpty())
                    return@observeOnce

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
            })
        } catch (_: NullPointerException) {
            loadingSpinner.visibility = GONE
            showSnackbarMessage(R.string.ProductNotFoundException)
            goBack()
            return
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_favorite -> setFavorite(true)
            R.id.action_remove_favorite -> setFavorite(false)
            R.id.action_send_message -> sendMessage()
            R.id.action_statistics -> {
                if(product != null)
                    navigateTo(R.id.from_product_to_statistics)
                else
                    showSnackbarMessage(R.string.NotFoundException)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sendMessage(): Boolean {
        // If the user can't be found
        if (!viewModel.isUserSignedIn || user == null) {
            showSnackbarMessage(R.string.SignedOutException)
            return true
        }

        try {
            if(user!!.id == product!!.ownerId) {
                showSnackbarMessage(R.string.SelfMessageException)
                return true
            }

            // Else, create a new discussion
            val discussion = Discussion(
                userId = user!!.id!!,
                ownerId = product!!.ownerId,
                productId = product!!.id!!,
                productName = product!!.title
            )

            if (discussions == null) {
                showSnackbarMessage(R.string.NetworkException)
                return true
            }

            val discussionId = viewModel.getDiscussionId(discussion, discussions!!)

            if (discussionId != null)
            // Else, go to discussion fragment
                navigateTo(R.id.from_product_to_messages, KEY_DISCUSSION_ID to discussionId)
            else
                viewModel.addDiscussion(discussion).observeOnce(viewLifecycleOwner, { (exception, discussion) ->
                    // If the call failed: show error message
                    if(exception != -1) {
                        showSnackbarMessage(exception)
                        return@observeOnce
                    }

                    // Else, go to discussion fragment
                    navigateTo(R.id.from_product_to_messages, KEY_DISCUSSION_ID to discussion.id!!)
                })
        } catch (_: NullPointerException) {
            showSnackbarMessage(R.string.NetworkException)
            return true
        }

        return true
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

    private fun setFavoriteItemVisibility() {
        if (user!!.favoritesIds.contains(product!!.id!!)) {
            setMenuItemVisibile(R.id.action_add_favorite, false)
            setMenuItemVisibile(R.id.action_remove_favorite, true)
        } else {
            setMenuItemVisibile(R.id.action_remove_favorite, false)
            setMenuItemVisibile(R.id.action_add_favorite, true)
        }
    }

    private fun setFavorite(setFavorite: Boolean): Boolean {
        // If the user can't be found
        if (!viewModel.isUserSignedIn || user == null) {
            showSnackbarMessage(R.string.SignedOutException)
            return true
        }

        // Update favorites
        try {
            val favoritesIds = user!!.favoritesIds
            val productId = product!!.id!!

            if (setFavorite && !favoritesIds.contains(productId))
                viewModel.addToFavorites(user!!.id!!, productId)
            else if (!setFavorite && favoritesIds.contains(productId))
                viewModel.removeFromFavorites(user!!.id!!, productId)
            else
                return true
        } catch (_: NullPointerException) {
            showSnackbarMessage(R.string.NetworkException)
        }

        return true
    }

}