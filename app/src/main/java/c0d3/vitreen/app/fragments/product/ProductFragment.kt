package c0d3.vitreen.app.fragments.product

import android.content.Intent
import android.net.Uri
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
    private var hasConsulted = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set elements visibility (while loading)
        productDetails.visibility = GONE
        buttonPreviousImage.visibility = GONE
        buttonNextImage.visibility = GONE
        setMenuItemVisibile(R.id.add_to_favorites, false)
        setMenuItemVisibile(R.id.remove_from_favorites, false)
        setMenuItemVisibile(R.id.send_message, false)
        setMenuItemVisibile(R.id.contact_owner, false)
        setMenuItemVisibile(R.id.show_statistics, false)
        setMenuItemVisibile(R.id.report_product, false)
        setMenuItemVisibile(R.id.delete_product, false)

        try {
            viewModel.discussions.observe(viewLifecycleOwner, { (exception, discussions) ->
                this.discussions = if (exception == -1) discussions else null
            })

            viewModel.getProduct().observeOnce(viewLifecycleOwner, { (exception, product, images) ->
                this.product = product

                if(exception != -1)
                    showSnackbarMessage(exception)

                viewModel.user.observe(viewLifecycleOwner, { (exception, user) ->
                    // When the call finishes, hide loading spinner
                    loadingSpinner.visibility = GONE

                    fillProductDetails(product)

                    if(exception == -1) {
                        this.user = user

                        setFavoriteItemVisibility(user, product)

                        if(user.id == product.ownerId) {
                            setMenuItemVisibile(R.id.send_message, false)
                            setMenuItemVisibile(R.id.contact_owner, false)
                            setMenuItemVisibile(R.id.report_product, false)
                            setMenuItemVisibile(R.id.delete_product, true)

                            if(user.isProfessional)
                                setMenuItemVisibile(R.id.show_statistics, true)
                            else
                                setMenuItemVisibile(R.id.show_statistics, false)
                        } else {
                            setMenuItemVisibile(R.id.send_message, true)
                            setMenuItemVisibile(R.id.contact_owner, true)
                            setMenuItemVisibile(R.id.show_statistics, false)
                            setMenuItemVisibile(R.id.report_product, true)
                            setMenuItemVisibile(R.id.delete_product, false)
                        }
                    } else {
                        this.user = null
                        setMenuItemVisibile(R.id.add_to_favorites, false)
                        setMenuItemVisibile(R.id.remove_from_favorites, false)
                        setMenuItemVisibile(R.id.send_message, false)
                        setMenuItemVisibile(R.id.contact_owner, true)
                        setMenuItemVisibile(R.id.show_statistics, false)
                        setMenuItemVisibile(R.id.report_product, false)
                        setMenuItemVisibile(R.id.delete_product, false)
                    }

                    if (!hasConsulted) {
                        hasConsulted = true
                        val city = user.location.city
                        val consultation = Consultation(city = city)

                        product.id?.let { id -> viewModel.addConsultation(id, consultation) }
                    }
                })

                if(exception != -1)
                    showSnackbarMessage(exception)

                // Check if there are loaded images
                if(images.isEmpty())
                    return@observeOnce

                // Display product images (first one)
                imageIndex = 0
                imageViewProduct.setImageBitmap(images[imageIndex])

                // Show previous and next buttons to switch between images
                if(images.size < 2)
                    return@observeOnce

                buttonPreviousImage.visibility = VISIBLE
                buttonNextImage.visibility = VISIBLE

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
        } catch (_: NullPointerException) {
            showSnackbarMessage(R.string.ProductNotFoundException)
            goBack()
            return
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_to_favorites -> setFavorite(true)
            R.id.remove_from_favorites -> setFavorite(false)
            R.id.send_message -> sendMessage()
            R.id.contact_owner -> contactOwner()
            R.id.show_statistics -> showStatistics()
            R.id.report_product -> reportProduct()
            R.id.delete_product -> deleteProduct()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteProduct(): Boolean {
        try {
            viewModel.deleteProduct(product!!.id!!, product!!.imagesPaths).observeOnce(viewLifecycleOwner, { exception ->
                if(exception != -1) {
                    showSnackbarMessage(exception)
                    return@observeOnce
                }

                showSnackbarMessage(R.string.product_deleted)
                goBack()
            })
        } catch(_: NullPointerException) {
            showSnackbarMessage(R.string.ProductNotDeletedException)
        }

        return true
    }

    private fun showStatistics(): Boolean {
        try {
            if(product != null && user!!.isProfessional)
                navigateTo(R.id.from_product_to_statistics)
            else
                showSnackbarMessage(R.string.NotFoundException)
        } catch(_: NullPointerException) {
            showSnackbarMessage(R.string.NotFoundException)
        }

        return true
    }

    private fun reportProduct(): Boolean {
        // If the user can't be found
        if (!viewModel.isUserSignedIn || user == null) {
            showSnackbarMessage(R.string.SignedOutException)
            return true
        }

        try {
            viewModel.reportProduct(product!!.id!!, user!!.id!!).observeOnce(viewLifecycleOwner, { exception ->
                // If the call failed: show error message
                if(exception != -1) {
                    showSnackbarMessage(exception)
                    return@observeOnce
                }

                showSnackbarMessage(R.string.product_reported)
            })
        } catch(_: NullPointerException) {
            showSnackbarMessage(R.string.NetworkException)
        }

        return true
    }

    private fun contactOwner(): Boolean {
        try {
            viewModel.getUserById(product!!.ownerId).observeOnce(viewLifecycleOwner, { (exception, user) ->
                // If the call failed: show error message
                if(exception != -1) {
                    showSnackbarMessage(exception)
                    return@observeOnce
                }

                if(user.contactByPhone)
                    sendSMS(user.phoneNumber, product!!.title)
                else
                    sendEmail(user.emailAddress, product!!.title)

            })
        } catch (_: NullPointerException) {
            showSnackbarMessage(R.string.NetworkException)
        }

        return true
    }

    private fun sendSMS(phoneNumber: String, productName: String) {
        val uri = Uri.parse("smsto:${phoneNumber}")
        val intent = Intent(Intent.ACTION_SENDTO, uri)

        intent.putExtra("sms_body", getString(R.string.about_product, productName))

        try {
            startActivity(intent)
        } catch (_: Exception) {
            showSnackbarMessage(R.string.error_placeholder)
        }
    }

    private fun sendEmail(emailAddress: String, productName: String) {
        val intent = Intent(Intent.ACTION_SEND)

        intent.data = Uri.parse("mailto:")
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_product, productName))

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
        } catch (_: Exception) {
            showSnackbarMessage(R.string.error_placeholder)
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

        // Show optional fields (if they exist)
        if(product.brand != null) {
            textViewBrand.text = product.brand
            textViewBrand.visibility = VISIBLE
        } else
            textViewBrand.visibility = GONE

        if(product.size != null) {
            textViewDimensions.text = product.size
            textViewDimensions.visibility = VISIBLE
        } else
            textViewDimensions.visibility = GONE

        if(user?.id == product.ownerId) {
            textViewReference.text = product.id
            textViewReference.visibility = VISIBLE
        } else {
            textViewReference.visibility = GONE
        }

        // Show product details
        productDetails.visibility = VISIBLE
    }

    private fun setFavoriteItemVisibility(user: User, product: Product) {
        try {
            if (user.favoritesIds.contains(product.id!!)) {
                setMenuItemVisibile(R.id.add_to_favorites, false)
                setMenuItemVisibile(R.id.remove_from_favorites, true)
            } else {
                setMenuItemVisibile(R.id.remove_from_favorites, false)
                setMenuItemVisibile(R.id.add_to_favorites, true)
            }
        } catch(_: NullPointerException) {
            setMenuItemVisibile(R.id.remove_from_favorites, false)
            setMenuItemVisibile(R.id.add_to_favorites, false)
            showSnackbarMessage(R.string.error_placeholder)
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