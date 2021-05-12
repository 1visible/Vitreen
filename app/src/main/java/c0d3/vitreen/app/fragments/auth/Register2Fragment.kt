package c0d3.vitreen.app.fragments.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.app.ActivityCompat
import c0d3.vitreen.app.R
import c0d3.vitreen.app.listeners.FetchLocation
import c0d3.vitreen.app.listeners.OnLocationFetchListener
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.User
import c0d3.vitreen.app.utils.Constants.Companion.KEY_EMAIL
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_adding1.*
import kotlinx.android.synthetic.main.fragment_register2.*
import kotlinx.android.synthetic.main.fragment_register2.editTextLocation
import java.lang.NullPointerException
import java.util.*

class Register2Fragment : VFragment(
    layoutId = R.layout.fragment_register2,
    topIcon = R.drawable.bigicon_authentification
) {

    private var emailAddress: String? = null
    private var locationGPS = Location()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        emailAddress = arguments?.getString(KEY_EMAIL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if argument could be retrieved
        if(emailAddress == null) {
            showMessage()
            navigateTo(R.id.from_register2_to_register1)
            return
        }

        // Try to initialize location from GPS
        context?.let { context -> initializeLocation(context) }

        // Toggle professional section visibility on switch change
        switchProfessionalAccount.setOnCheckedChangeListener { _, isChecked ->
            val visibility = if(isChecked) VISIBLE else GONE
            editTextCompany.visibility = visibility
            editTextSiret.visibility = visibility
        }

        // On submit button click, create user account
        buttonSubmitRegister.setOnClickListener {
            // Check if required inputs are filled
            if(isAnyRequiredInputEmpty(editTextUsername, editTextPhoneNumber, editTextLocation))
                return@setOnClickListener
            if(switchProfessionalAccount.isChecked && isAnyRequiredInputEmpty(editTextCompany, editTextSiret))
                return@setOnClickListener

            val username = inputToString(editTextUsername)
            val phoneNumber = inputToString(editTextPhoneNumber)
            val locationName = inputToString(editTextLocation)?.toLowerCase(Locale.ROOT)?.capitalize(Locale.ROOT)
            val company = inputToString(editTextCompany)
            val siret = inputToString(editTextSiret)

            // Double check personal informations after conversion
            if (username == null || phoneNumber == null || locationName == null) {
                showMessage()
                return@setOnClickListener
            }

            // Double check professional informations after conversion
            if (switchProfessionalAccount.isChecked && (company == null || siret == null)) {
                showMessage()
                return@setOnClickListener
            }

            // Get location from city name
            viewModel.getLocation(locationName).observeOnce(viewLifecycleOwner, { pair ->
                val exception = pair.first
                var location = pair.second
                val zipCodeL = if(location.city == locationGPS.city) locationGPS.zipCode else null
                // If the call fails, show error message and hide loading spinner
                if(exception != R.string.error_404 && handleError(exception)) return@observeOnce

                // Else if location could not be found, create new location
                if(exception == R.string.error_404) {
                    location = Location(locationName, zipCodeL)
                    viewModel.addLocation(location)
                }
                // Else if location has no zip code, update it
                else if(location.zipCode == null && zipCodeL != null) {
                    location.zipCode = zipCodeL
                    location.id?.let { id -> viewModel.updateLocation(id, zipCodeL) }
                }

                try {
                    // Create user profile with filled informations
                    val user = User(
                        username = username,
                        emailAddress = emailAddress!!,
                        phoneNumber = phoneNumber,
                        location = location,
                        contactByPhone = radioButtonPhone.isChecked,
                        isProfessional = switchProfessionalAccount.isChecked,
                        companyName = company,
                        siretNumber = siret
                    )

                    // Register user profile to database
                    viewModel.addUser(user).observeOnce(viewLifecycleOwner, observeOnce2@ { exception2 ->
                        // If the call fails, show error message and hide loading spinner
                        if(handleError(exception2)) return@observeOnce2

                        // Else, navigate to profile fragment
                        navigateTo(R.id.from_register2_to_profile)
                        showMessage(R.string.register_success)
                    })
                } catch (_: NullPointerException) {
                    showMessage()
                    navigateTo(R.id.from_register2_to_register1)
                }
            })
        }
    }

    private fun initializeLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            FetchLocation().setOnLocationFetchListner(getLocationListener(), context)
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