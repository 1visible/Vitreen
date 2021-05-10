package c0d3.vitreen.app.fragments.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.core.app.ActivityCompat
import c0d3.vitreen.app.R
import c0d3.vitreen.app.listeners.FetchLocation
import c0d3.vitreen.app.listeners.OnLocationFetchListener
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.User
import c0d3.vitreen.app.utils.Constants.Companion.LOCALISATION_REQUEST
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_register2.*
import java.io.IOException
import java.util.*

class Register2Fragment : VFragment(
    R.layout.fragment_register2,
    R.drawable.bigicon_authentification,
    -1
) {
    private lateinit var emailAddress: String
    private var cityName = ""
    private var zipCode = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        emailAddress = arguments?.getString("email").orEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { initializeLocation(it) }

        switchProfessionalAccount.setOnCheckedChangeListener { _, isChecked ->
            val visibility = if(isChecked) VISIBLE else GONE
            editTextCompany.visibility = visibility
            editTextSiret.visibility = visibility
        }

        buttonSubmitRegister.setOnClickListener {
            // Check if required inputs are filled
            if(isAnyRequiredInputEmpty(editTextFullname, editTextPhoneNumber, editTextLocation))
                return@setOnClickListener
            if(switchProfessionalAccount.isChecked && isAnyRequiredInputEmpty(editTextCompany, editTextSiret))
                return@setOnClickListener

            val fullname = inputToString(editTextFullname)
            val phoneNumber = inputToString(editTextPhoneNumber)
            val locationName = inputToString(editTextLocation)?.toLowerCase(Locale.ROOT)?.capitalize(Locale.ROOT)
            val zipCodeL = zipCode.toLongOrNull()
            val company = inputToString(editTextCompany)
            val siret = inputToString(editTextSiret)

            // Double check informations after conversion
            if(fullname == null || phoneNumber == null || locationName == null)
                return@setOnClickListener
            if(switchProfessionalAccount.isChecked && (company == null || siret == null))
                return@setOnClickListener

            viewModel.getLocation(locationName).observeOnce(viewLifecycleOwner, { pair ->
                val errorCode = pair.first
                var location = pair.second
                // If the call fails, show error message and hide loading spinner
                if(errorCode != R.string.error_404 && handleError(errorCode)) return@observeOnce

                // Else if location could not be found, create new location
                if(errorCode == R.string.error_404)
                    location = Location(locationName, zipCodeL)
                else if(location.zipCode == null && zipCodeL != null) {
                    location.zipCode = zipCodeL
                    viewModel.updateLocation(location.id, zipCodeL)
                }

                val user = User(
                    fullname = fullname,
                    emailAddress = emailAddress,
                    phoneNumber = phoneNumber,
                    location = location,
                    contactByPhone = radioButtonPhone.isChecked,
                    isProfessional = switchProfessionalAccount.isChecked,
                    companyName = company,
                    siretNumber = siret
                )

                viewModel.addUser(user).observeOnce(viewLifecycleOwner, observeOnce2@ { errorCode2 ->
                    // If the call fails, show error message and hide loading spinner
                    if(handleError(errorCode2)) return@observeOnce2
                    // Else, navigate to Register2 fragment
                    navigateTo(R.id.action_navigation_register2_to_navigation_profil)
                    // TODO : Ajouter message confirmation
                })
            })
        }
    }

    private fun initializeLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCALISATION_REQUEST
                )
            }
        }

        val fetchLocation = FetchLocation()
        val listern = object : OnLocationFetchListener {
            override fun onComplete(location: android.location.Location?) {
                val geocoder = Geocoder(context, Locale.getDefault())
                try {

                    val adresse = location?.let {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    }

                    if (adresse != null) {
                        cityName = adresse[0].locality
                        zipCode = adresse[0].postalCode
                        editTextLocation.editText?.text?.clear()
                        editTextLocation.editText?.setText(adresse[0].locality)
                    }

                } catch (e: IOException) {
                    Toast.makeText(context, getString(R.string.errorMessage), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailed(e: String?) {
                Toast.makeText(context, getString(R.string.errorMessage), Toast.LENGTH_SHORT).show()
            }

        }
        fetchLocation.setOnLocationFetchListner(listern, context)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCALISATION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    editTextLocation.editText?.text?.clear()
                    Toast.makeText(context, getString(R.string.errorMessage), Toast.LENGTH_SHORT)
                        .show() // TODO : Replace this soon
                } else
                    navigateTo(R.id.action_navigation_register2_self, "email" to emailAddress)
            }
        }
    }

}