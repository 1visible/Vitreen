package c0d3.vitreen.app.fragments.adding

import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants.Companion.GALLERY_REQUEST
import c0d3.vitreen.app.utils.Constants.Companion.IMAGES_LIMIT_PROFESSIONAL
import c0d3.vitreen.app.utils.Constants.Companion.IMAGES_LIMIT_USER
import c0d3.vitreen.app.utils.Constants.Companion.KEY_CATEGORY
import c0d3.vitreen.app.utils.Constants.Companion.KEY_DESCRIPTION
import c0d3.vitreen.app.utils.Constants.Companion.KEY_LOCATION
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRICE
import c0d3.vitreen.app.utils.Constants.Companion.KEY_TITLE
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_adding1.*
import kotlinx.android.synthetic.main.fragment_adding2.*
import kotlinx.android.synthetic.main.fragment_adding2.buttonNextImage
import kotlinx.android.synthetic.main.fragment_adding2.buttonPreviousImage
import kotlinx.android.synthetic.main.fragment_adding2.imageViewProduct
import kotlinx.android.synthetic.main.fragment_product.*
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


class Adding2Fragment : VFragment(
    layoutId = R.layout.fragment_adding2,
    topIcon = R.drawable.bigicon_adding,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_adding,
    requireAuth = true,
    loginNavigationId = R.id.from_adding2_to_home
) {

    private var title: String? = null
    private var description: String? = null
    private var price: Double? = null
    private var category: Category? = null
    private var location: Location? = null
    private var imageIndex = 0
    private var nbImagesMax = IMAGES_LIMIT_USER
    private var uriList = ArrayList<Uri>()
    private var inputStreamList = ArrayList<InputStream>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        title = arguments?.getString(KEY_TITLE)
        description = arguments?.getString(KEY_DESCRIPTION)
        price = arguments?.getString(KEY_PRICE)?.toDoubleOrNull()
        category = arguments?.get(KEY_CATEGORY) as? Category?
        location = arguments?.get(KEY_LOCATION) as? Location?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If user is not signed in, skip this part
        if(!isUserSignedIn())
            return

        // Check if arguments could be retrieved
        if(title == null
            || description == null
            || price == null
            || category == null
            || category !is Category
            || location == null
            || location !is Location) {
            showMessage()
            navigateTo(R.id.from_adding2_to_adding1)
            return
        }

        // Get current user informations
        try {
            viewModel.getUser(user!!).observeOnce(viewLifecycleOwner, { pair ->
                val exception = pair.first
                val user = pair.second
                // If the call fails, show error message, hide loading spinner and show empty view
                if(handleError(exception, R.string.no_products)) return@observeOnce

                // Else, set images max for this user
                nbImagesMax = if(user.isProfessional) IMAGES_LIMIT_PROFESSIONAL else IMAGES_LIMIT_USER

                buttonConfirmation.setOnClickListener {
                    val brand = inputToString(editTextBrand)
                    val size = inputToString(editTextDimensions)

                    // Create product to add with informations
                    try {
                        val product = Product(
                            title = title!!,
                            description = description!!,
                            price = price!!,
                            brand = brand,
                            size = size,
                            location = location!!,
                            category = category!!,
                            ownerId = user.id!!
                        )

                        viewModel.addProduct(product, inputStreamList, viewLifecycleOwner).observeOnce(viewLifecycleOwner,  observeOnce2@ { pair ->
                            val exception2 = pair.first
                            // If the call fails, show error message and hide loading spinner
                            if(handleError(exception2)) return@observeOnce2

                            // Else, navigate to home fragment and show confirmation message
                            navigateTo(R.id.from_adding2_to_home)
                            showMessage(R.string.add_product_success)
                        })
                    } catch (_: NullPointerException) {
                        showMessage()
                        navigateTo(R.id.from_adding2_to_adding1)
                    }
                }
            })
        } catch (_: NullPointerException) {
            showMessage()
            navigateTo(R.id.from_adding2_to_adding1)
            return
        }

        // On add image button click, open the gallery (or file system) and allow images picking
        buttonAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST)
        }

        // On previous button click, go to previous image
        buttonPreviousImage.setOnClickListener {
            if(uriList.isEmpty())
                return@setOnClickListener

            imageIndex = if (imageIndex <= 0) (uriList.size - 1) else imageIndex--
            imageViewProduct.setImageURI(uriList[imageIndex])
        }

        // On next button click, go to next image
        buttonNextImage.setOnClickListener {
            if(uriList.isEmpty())
                return@setOnClickListener

            imageIndex = if (imageIndex >= uriList.size - 1) 0 else imageIndex++
            imageViewProduct.setImageURI(uriList[imageIndex])
        }

        // On remove button click, remove image
        buttonRemoveImage.setOnClickListener {
            if(uriList.isEmpty())
                return@setOnClickListener

            uriList.removeAt(imageIndex)
            inputStreamList.removeAt(imageIndex)
            imageIndex = 0

            // Hide slider controls if there are no images left
            if(uriList.isEmpty()) {
                buttonRemoveImage.visibility = GONE
                buttonNextImage.visibility = GONE
                buttonPreviousImage.visibility = GONE
                imageViewProduct.setImageResource(R.drawable.image_placeholder)
            } else
                imageViewProduct.setImageURI(uriList[0])
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if gallery images picking has finished and picked images
        try {
            if (requestCode != GALLERY_REQUEST
                || resultCode != RESULT_OK
                || data == null
                || (data.data == null && data.clipData == null)
                || (data.clipData != null && data.clipData!!.itemCount < 1))
                return
        } catch (_: NullPointerException) {
            showMessage()
            return
        }

        imageIndex = 0
        val tempUriList = ArrayList<Uri>()
        val tempInputStreamList = ArrayList<InputStream>()

        // Put images URI in a list
        if(data.clipData != null) {
            val clipData: ClipData = data.clipData!!
            for(i in 0 until clipData.itemCount)
                tempUriList.add(clipData.getItemAt(i).uri)
        } else if(data.data != null)
            tempUriList.add(data.data!!)

        // Check if the new images size does not exceed the max
        if(uriList.size + tempUriList.size > nbImagesMax) {
            showMessage(R.string.max_images_error)
            return
        }

        // Get streams for images URI
        tempUriList.forEach { uri ->
            context?.contentResolver?.openInputStream(uri)?.let { inputSteam ->
                tempInputStreamList.add(inputSteam)
            }
        }

        // Check if all streams could be retrieved
        if(tempUriList.size != tempInputStreamList.size) {
            showMessage()
            return
        }

        // Add new images to list
        uriList.addAll(tempUriList)
        inputStreamList.addAll(tempInputStreamList)

        // Show controls and display first image if there are images to show
        if(uriList.isNotEmpty()) {
            buttonRemoveImage.visibility = VISIBLE
            buttonNextImage.visibility = VISIBLE
            buttonPreviousImage.visibility = VISIBLE
            imageViewProduct.setImageURI(uriList[0])
        }
    }
}