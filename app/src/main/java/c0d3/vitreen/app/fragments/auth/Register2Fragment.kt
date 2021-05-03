package c0d3.vitreen.app.fragments.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
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
        switchProfessionalAccount.isChecked = false
        editTextCompany.visibility = View.GONE
        editTextSiret.visibility = View.GONE
        switchProfessionalAccount.setOnCheckedChangeListener { _, isChecked ->
            editTextCompany.visibility = if (isChecked) View.VISIBLE else View.GONE
            editTextSiret.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        buttonSubmitRegister.setOnClickListener {
            var user: User
            val currentLocation = Location(
                editTextLocation.editText?.text.toString().replaceFirst(
                    editTextLocation.editText?.text.toString()[0],
                    editTextLocation.editText?.text.toString()[0].toUpperCase()
                ),
                if (zipCode == "" || editTextLocation.editText?.text.toString() != cityName) null else zipCode.toInt()
            )

            locationsCollection.whereEqualTo("name", currentLocation.name)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.size() == 1) {
                        for (document in documents) {
                            if (document.get("zipCode") == null) {
                                locationsCollection
                                    .document(document.id)
                                    .update("zipCode", currentLocation.zipCode)
                            }
                            user = if (switchProfessionalAccount.isChecked) {
                                User(
                                    editTextFullname.editText?.text.toString(),
                                    emailAddress,
                                    editTextPhoneNumber.editText?.text.toString(),
                                    radioButtonPhone.isChecked,
                                    true,
                                    document.id,
                                    editTextCompany.editText?.text.toString(),
                                    editTextSiret.editText?.text.toString()
                                )
                            } else {
                                User(
                                    editTextFullname.editText?.text.toString(),
                                    emailAddress,
                                    editTextPhoneNumber.editText?.text.toString(),
                                    radioButtonPhone.isChecked,
                                    false,
                                    document.id
                                )
                            }
                            usersCollection.document().set(user).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navigateTo(R.id.action_navigation_register2_to_navigation_profil)
                                } else
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.errorMessage),
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }

                        }
                    } else {
                        locationsCollection.add(currentLocation)
                            .addOnSuccessListener {
                                user = if (switchProfessionalAccount.isChecked) {
                                    User(
                                        editTextFullname.editText?.text.toString(),
                                        emailAddress,
                                        editTextPhoneNumber.editText?.text.toString(),
                                        radioButtonPhone.isChecked,
                                        true,
                                        it.id,
                                        editTextCompany.editText?.text.toString(),
                                        editTextSiret.editText?.text.toString()
                                    )
                                } else {
                                    User(
                                        editTextFullname.editText?.text.toString(),
                                        emailAddress,
                                        editTextPhoneNumber.editText?.text.toString(),
                                        radioButtonPhone.isChecked,
                                        false,
                                        it.id
                                    )
                                }

                                usersCollection.document().set(user).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        navigateTo(R.id.action_navigation_register2_to_navigation_profil)
                                    } else {
                                        showError(R.string.errorMessage)
                                    }
                                }
                            }
                            .addOnFailureListener {
                                showError(R.string.errorMessage)
                            }
                    }
                }
                .addOnFailureListener {
                    showError(R.string.errorMessage)
                }
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
                        .show()
                } else {
                    val bundle = bundleOf("email" to emailAddress)
                    findNavController().navigate(R.id.action_navigation_register2_self, bundle)
                }
            }
        }
    }

}