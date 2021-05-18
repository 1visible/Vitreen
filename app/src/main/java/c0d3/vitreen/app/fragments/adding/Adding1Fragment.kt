package c0d3.vitreen.app.fragments.adding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.listeners.FetchLocation
import c0d3.vitreen.app.listeners.OnLocationFetchListener
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.utils.Constants.Companion.KEY_CATEGORY
import c0d3.vitreen.app.utils.Constants.Companion.KEY_DESCRIPTION
import c0d3.vitreen.app.utils.Constants.Companion.KEY_LOCATION
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRICE
import c0d3.vitreen.app.utils.Constants.Companion.KEY_TITLE
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_adding1.*
import kotlinx.android.synthetic.main.fragment_adding1.editTextLocation
import kotlinx.android.synthetic.main.fragment_register1.*
import kotlinx.android.synthetic.main.fragment_register2.*
import java.util.*

class Adding1Fragment : VFragment(
    layoutId = R.layout.fragment_adding1,
    topIcon = R.drawable.bigicon_adding,
    requireAuth = true,
    loginNavigationId = R.id.from_adding1_to_login
) {

    private var categories: List<Category>? = null
    private var locationGPS = Location()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If user is not signed in, skip this part
        if (!viewModel.isUserSignedIn)
            return

        // Try to initialize location from GPS
        context?.let { context -> initializeLocation(context) }

        // Get categories to put them as edit text choices
        viewModel.categories.observe(viewLifecycleOwner, { (exception, categories) ->
            // If the call fails, show error message
            if(exception != -1) {
                showSnackbarMessage(exception)
                return@observe
            }

            // Else, put categories as edit text choices
            this.categories = categories
            val categoryNames = categories.map { category -> category.name }
            val adapter = context?.let { context -> ArrayAdapter(context, R.layout.dropdown_menu_item, categoryNames) }

            (textInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        })

        // On adding (part 2) button click, navigate to Adding2 fragment
        buttonToAdding2.setOnClickListener {
            // Check if required inputs are filled
            if (isAnyRequiredInputEmpty(textInputCategory, editTextTitle, editTextPrice, editTextLocation, editTextDescription))
                return@setOnClickListener

            val categoryName = inputToString(textInputCategory)
            val locationName = inputToString(editTextLocation)?.toLowerCase(Locale.ROOT)?.capitalize(Locale.ROOT)
            var categoryDTO: Category? = null

            // Double check category and location after conversion
            if (categoryName == null || locationName == null) {
                showSnackbarMessage(R.string.error_placeholder)
                return@setOnClickListener
            }

            // Get category according to input category
            categories?.forEach { category ->
                if (category.name == categoryName) {
                    categoryDTO = category
                    return@forEach
                }
            }

            // Check if category could be retrieved
            if (categoryDTO == null) {
                showSnackbarMessage(R.string.NetworkException)
                return@setOnClickListener
            }

            // Get location from city name
            viewModel.getLocation(locationName).observeOnce(viewLifecycleOwner, { (exception, location) ->
                val zipCode = if(location.city == locationGPS.city) locationGPS.zipCode else null
                // If the call fails, show error message
                if(exception != -1 && exception != R.string.NotFoundException) {
                    showSnackbarMessage(exception)
                    return@observeOnce
                }

                // Else if location could not be found, create new location
                if(exception == R.string.NotFoundException) {
                    location.city = locationName
                    location.zipCode = zipCode
                    viewModel.addLocation(location)
                }
                // Else if location has no zip code, update it
                else if(location.zipCode == null && zipCode != null) {
                    location.zipCode = zipCode
                    location.id?.let { id -> viewModel.updateLocation(id, zipCode) }
                }

                // Navigate to adding (part 2) with product informations
                navigateToAdding2(categoryDTO, location)
            })
        }

    }

    private fun initializeLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        FetchLocation().setOnLocationFetchListner(getLocationListener(), context)
    }

    private fun navigateToAdding2(category: Category?, location: Location) {
        if(category == null) {
            showSnackbarMessage(R.string.error_placeholder)
            return
        }

        navigateTo(
            R.id.from_adding1_to_adding2,
            KEY_CATEGORY to category,
            KEY_TITLE to inputToString(editTextTitle),
            KEY_PRICE to inputToString(editTextPrice),
            KEY_LOCATION to location,
            KEY_DESCRIPTION to inputToString(editTextDescription)
        )
    }

    private fun getLocationListener(): OnLocationFetchListener {
        return object : OnLocationFetchListener {
            override fun onComplete(location: android.location.Location?) {
                super.onComplete(location)

                try {
                    // Get location with GPS
                    val address = location?.let {
                        Geocoder(requireContext(), Locale.getDefault()).getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                    }

                    // Check if location could be retrieved
                    if(address.isNullOrEmpty())
                        return

                    val city = address.first().locality
                    val zipCode = address.first().postalCode

                    // Update locationGPS variable with retrieved values
                    locationGPS.city = city
                    locationGPS.zipCode = zipCode.toLongOrNull()

                    // Change edit text location with new location
                    editTextLocation?.editText?.setText(city)
                } catch (_: Exception) { }
            }
        }
    }

}