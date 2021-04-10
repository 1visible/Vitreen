package c0d3.vitreen.app.fragments.auth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.fragments.adding.Adding1Fragment
import c0d3.vitreen.app.fragments.adding.Adding2Fragment
import c0d3.vitreen.app.fragments.profile.ProfileFragment
import c0d3.vitreen.app.listeners.FetchLocation
import c0d3.vitreen.app.listeners.OnLocationFetchListner
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.User
import c0d3.vitreen.app.utils.ChildFragment
import c0d3.vitreen.app.utils.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_register2.*
import java.io.IOException
import java.util.*

class Register2Fragment : ChildFragment() {
    private val db = Firebase.firestore
    private val locations = db.collection("locations")
    private var emailAddress: String = ""

    private var cityName = ""
    private var zipCode = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString(Constants.KEYEMAIL)?.let {
            emailAddress = it
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register2, container, false)
    }

    // TODO: Remove this if not needed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { initializeLocation(it) }
        switchProfessionalAccount.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editTextCompany.visibility = View.VISIBLE
                editTextSiret.visibility = View.VISIBLE
            } else {
                editTextCompany.visibility = View.GONE
                editTextSiret.visibility = View.GONE
            }
        }

        buttonSubmitRegister.setOnClickListener {
            var user: User
            val currentLocation = Location(
                    editTextLocation.text.toString(),
                    if (zipCode == "") null else zipCode.toInt()
            )

            locations
                    .whereEqualTo("name", currentLocation.name)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.size() == 1) {
                            for (document in documents) {
                                if (document.get("zipCode") == null) {
                                    locations
                                            .document(document.id)
                                            .update("zipCode", currentLocation.zipCode)
                                }
                                if (switchProfessionalAccount.isChecked) {
                                    user = User(
                                            editTextFullname.text.toString(),
                                            emailAddress,
                                            editTextPhoneNumber.text.toString(),
                                            radioButtonPhone.isChecked,
                                            true,
                                            document.id,
                                            editTextCompany.text.toString(),
                                            editTextSiret.text.toString()
                                    )
                                } else {
                                    user = User(
                                            editTextFullname.text.toString(),
                                            emailAddress,
                                            editTextPhoneNumber.text.toString(),
                                            radioButtonPhone.isChecked,
                                            false,
                                            document.id
                                    )
                                }

                                db.collection("Users").document().set(user).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(requireContext(), getString(R.string.inscriptionOk), Toast.LENGTH_SHORT).show()
                                        // TODO: Remplacer par la navigation
                                        parentFragmentManager
                                                .beginTransaction()
                                                .replace(R.id.nav_host_fragment, ProfileFragment.newInstance())
                                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                                .commit()
                                    } else
                                        Toast.makeText(requireContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()
                                }

                            }
                        } else {
                            locations.add(currentLocation)
                                    .addOnSuccessListener {
                                        if (switchProfessionalAccount.isChecked) {
                                            user = User(
                                                    editTextFullname.text.toString(),
                                                    emailAddress,
                                                    editTextPhoneNumber.text.toString(),
                                                    radioButtonPhone.isChecked,
                                                    true,
                                                    it.id,
                                                    editTextCompany.text.toString(),
                                                    editTextSiret.text.toString()
                                            )
                                        } else {
                                            user = User(
                                                    editTextFullname.text.toString(),
                                                    emailAddress,
                                                    editTextPhoneNumber.text.toString(),
                                                    radioButtonPhone.isChecked,
                                                    false,
                                                    it.id
                                            )
                                        }

                                        db.collection("Users").document().set(user).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(requireContext(), getString(R.string.inscriptionOk), Toast.LENGTH_SHORT).show()
                                                // TODO: Remplacer par la navigation
                                                parentFragmentManager
                                                        .beginTransaction()
                                                        .replace(R.id.nav_host_fragment, ProfileFragment.newInstance())
                                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                                        .commit()
                                            } else
                                                Toast.makeText(requireContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                                context,
                                                getString(R.string.ErrorMessage),
                                                Toast.LENGTH_SHORT
                                        ).show()
                                    }
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                                context,
                                getString(R.string.ErrorMessage),
                                Toast.LENGTH_SHORT
                        )
                                .show()
                    }


        }

    }

    private fun initializeLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        Constants.LocalisationCode
                )
            }
        }
        val fetchLocation = FetchLocation();
        val listern = object : OnLocationFetchListner {
            override fun onComplete(location: android.location.Location?) {
                val geocoder = Geocoder(context, Locale.getDefault())
                try {
                    val adresse = location?.let {
                        geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                        )
                    }
                    if (adresse != null) {
                        cityName = adresse[0].locality
                        zipCode = adresse[0].postalCode
                        editTextLocation.text.clear()
                        editTextLocation.text = adresse[0].locality.toEditable()
                    }
                } catch (e: IOException) {
                    Toast.makeText(context, getString(R.string.ErrorMessage), Toast.LENGTH_SHORT)
                            .show()

                }
            }

            override fun onFailed(e: String?) {
                Toast.makeText(context, getString(R.string.noLocation), Toast.LENGTH_SHORT).show()
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
            Constants.LocalisationCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    editTextLocation.text.clear()
                    Toast.makeText(
                            context,
                            getString(R.string.locationDenied),
                            Toast.LENGTH_SHORT
                    )
                            .show()
                } else {
                    parentFragmentManager
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment, newInstance(emailAddress))
                            .commit()
                }
            }
        }
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    companion object {
        @JvmStatic
        fun newInstance(email: String): Register2Fragment = Register2Fragment().apply {
            arguments = Bundle().apply { putString(Constants.KEYEMAIL, email) }
        }
    }

}