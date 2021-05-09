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
import c0d3.vitreen.app.listeners.FetchLocation
import c0d3.vitreen.app.listeners.OnLocationFetchListener
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.utils.Constants.Companion.KEY_CATEGORY
import c0d3.vitreen.app.utils.Constants.Companion.DESCRIPTION
import c0d3.vitreen.app.utils.Constants.Companion.LOCALISATION_REQUEST
import c0d3.vitreen.app.utils.Constants.Companion.KEY_LOCATION
import c0d3.vitreen.app.utils.Constants.Companion.PRICE
import c0d3.vitreen.app.utils.Constants.Companion.TITLE
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_adding1.*
import kotlinx.android.synthetic.main.fragment_register1.*
import java.util.*
import kotlin.collections.ArrayList

class Adding1Fragment : VFragment(
    R.layout.fragment_adding1,
    R.drawable.bigicon_adding,
    -1,
    true,
    R.menu.menu_adding,
    true,
    R.id.action_navigation_adding1_to_navigation_login
) {
    private var categoriesList = ArrayList<Category>()

    private var zipCode: String? = null
    private var cityName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { initializeLocation(it) }

        viewModel.getCategories().observeOnce(viewLifecycleOwner, { pair ->
            if (handleError(pair.first, R.string.errorMessage)) return@observeOnce
            categoriesList = pair.second as ArrayList<Category>
            // Ajout des catégories au menu déroulant du formulaire
            val adapter = context?.let { context ->
                ArrayAdapter(
                    context,
                    R.layout.dropdown_menu_item,
                    pair.second
                )
            }

            (textInputCategory?.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        }
        )

        // Bouton de navigation vers le formulaire d'ajout (2/2)
        buttonToAdding2.setOnClickListener {
            // Vérifie que les champs du formulaire ne sont pas vides
            if (isAnyInputEmpty(
                    textInputCategory,
                    editTextTitle,
                    editTextPrice,
                    editTextLocation,
                    editTextDescription
                )
            ) {
                showMessage(R.string.errorMessage)
                return@setOnClickListener
            }

            // Récupération de l'ID correspondant à la catégorie choisie
            var category: Category? = null

            categoriesList.forEach { currentCategory ->
                if (currentCategory.name == textInputCategory.editText?.text.toString())
                    category = currentCategory
            }

            if (category == null) {
                showMessage(R.string.errorMessage)
                return@setOnClickListener
            }

            // Navigation vers le formulaire d'ajout (2/2) après récupération de la localisation de l'annonce
            val currentLocation = Location(
                editTextLocation.editText?.text.toString().capitalize(Locale.getDefault()),
                if (cityName != editTextLocation.editText?.text.toString()) null else zipCode?.toLong()
            )
            // Récupération de la localisation renseignée
            viewModel.getLocations(currentLocation.name).observeOnce(viewLifecycleOwner, { pair ->
                if (handleError(pair.first, R.string.errorMessage)) return@observeOnce
                val location = pair.second
                if (location != null) {
                    if (location.zipCode == null) {
                        currentLocation.zipCode?.let { it1 ->
                            viewModel.updateLocation(
                                location.name,
                                it1
                            )
                        }
                    }
                    navigateToAdding2(category!!, currentLocation)
                } else {
                    viewModel.addLocation(currentLocation)
                    navigateToAdding2(category!!, currentLocation)
                }
            })
        }

    }

    // TODO : Virer peut-être
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
                    showMessage(R.string.errorMessage)
                    return
                }
                navigateTo(R.id.action_navigation_adding1_self)
            }
        }

    }

    private fun initializeLocation(context: Context) {
        // TODO : Virer peut-être (et garder FetchLocation)
        // Demande de permission pour la récupération de la localisation
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
        )
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCALISATION_REQUEST
            )

        // Listener de récupération de localisation
        FetchLocation().setOnLocationFetchListner(getLocationListener(), context)
    }

    private fun navigateToAdding2(category: Category, location: Location) {
        navigateTo(
            R.id.action_navigation_adding1_to_navigation_adding2,
            KEY_CATEGORY to category,
            TITLE to editTextTitle.editText?.text.toString(),
            PRICE to editTextPrice.editText?.text.toString(),
            KEY_LOCATION to location,
            DESCRIPTION to editTextDescription.editText?.text.toString()
        )
    }

    private fun getLocationListener(): OnLocationFetchListener {
        return object : OnLocationFetchListener {
            override fun onComplete(location: android.location.Location?) {
                super.onComplete(location)
                try {

                    val address = location?.let {
                        Geocoder(requireContext(), Locale.getDefault()).getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                    }

                    if (address != null) {
                        zipCode = address[0].postalCode
                        cityName = address[0].locality
                        editTextLocation.editText?.text?.clear()
                        editTextLocation.editText?.setText(address[0].locality)
                    }

                } catch (_: Exception) {
                    showMessage(R.string.errorMessage)
                }
            }

            override fun onFailed(e: String?) {
                super.onFailed(e)
                showMessage(R.string.errorMessage)
            }
        }
    }

}