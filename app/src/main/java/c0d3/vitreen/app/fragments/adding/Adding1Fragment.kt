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
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.dto.CategoryDTO
import c0d3.vitreen.app.utils.Constants.Companion.CATEGORY_ID
import c0d3.vitreen.app.utils.Constants.Companion.DESCRIPTION
import c0d3.vitreen.app.utils.Constants.Companion.LOCALISATION_REQUEST
import c0d3.vitreen.app.utils.Constants.Companion.LOCATION_ID
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
    private val categoriesList = ArrayList<CategoryDTO>()

    private var zipCode: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { initializeLocation(it) }

        // Récupération des catégories depuis la BDD
        categoriesCollection.get().addOnSuccessListener { documents ->

            // Ajout des catégories dans une liste exploitable
            documents.forEach { document ->
                val categoryDTO = CategoryDTO(document.id, document.get("name").toString())
                categoriesList.add(categoryDTO)
            }

            // Ajout des catégories au menu déroulant du formulaire
            val adapter = context?.let { context ->
                ArrayAdapter(
                    context,
                    R.layout.dropdown_menu_item,
                    categoriesList.map { it.DtoToModel().name })
            }

            (textInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        }

        // Bouton de navigation vers le formulaire d'ajout (2/2)
        buttonToAdding2.setOnClickListener {
            // Vérifie que les champs du formulaire ne sont pas vides
            if (isAnyInputEmpty(
                    textInputCategory.editText,
                    editTextTitle,
                    editTextPrice,
                    editTextLocation,
                    editTextDescription
                )
            ) {
                showError(R.string.errorMessage)
                return@setOnClickListener
            }

            // Récupération de l'ID correspondant à la catégorie choisie
            var categoryId: String? = null

            categoriesList.forEach { category ->
                if (category.name == textInputCategory.editText?.text.toString())
                    categoryId = category.id
            }

            if (categoryId == null) {
                showError(R.string.errorMessage)
                return@setOnClickListener
            }

            // Navigation vers le formulaire d'ajout (2/2) après récupération de la localisation de l'annonce
            val currentLocation = Location(
                editTextLocation.text.toString().capitalize(Locale.getDefault()),
                zipCode?.toInt()
            )
            // Récupération de la localisation renseignée
            locationsCollection.whereEqualTo("name", currentLocation.name).get()
                .addOnSuccessListener { documents ->

                    // Ajout du code postal à la localisation (s'il n'existe pas)
                    if (documents.size() > 0) {
                        val location = documents.first()
                        if (location.get("zipCode") == null)
                            locationsCollection.document(location.id)
                                .update("zipCode", currentLocation.zipCode)
                        // Navigation vers le formulaire d'ajout (2/2) en y passant les données
                        navigateToAdding2(categoryId, location.id)

                        // Ajout de la nouvelle localisation dans la BDD (si elle n'existe pas)
                    } else locationsCollection.add(currentLocation)
                        .addOnSuccessListener { location ->
                            // Navigation vers le formulaire d'ajout (2/2) en y passant les données
                            navigateToAdding2(categoryId, location.id)
                        }.addOnFailureListener {
                        showError(R.string.errorMessage)
                    }

                }.addOnFailureListener {
                showError(R.string.errorMessage)
            }

        }

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
                    editTextLocation.text.clear()
                    showError(R.string.errorMessage)
                    return
                }
                navigateTo(R.id.action_navigation_adding1_self)
            }
        }

    }

    private fun initializeLocation(context: Context) {
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

    private fun navigateToAdding2(categoryId: String?, locationId: String) {
        navigateTo(
            R.id.action_navigation_adding1_to_navigation_adding2,
            CATEGORY_ID to categoryId,
            TITLE to editTextTitle.text.toString(),
            PRICE to editTextPrice.text.toString(),
            LOCATION_ID to locationId,
            DESCRIPTION to editTextDescription.text.toString()
        )
    }

    private fun getLocationListener(): OnLocationFetchListener {
        return object : OnLocationFetchListener {
            override fun onComplete(location: android.location.Location?) {
                super.onComplete(location)
                try {

                    val address = location?.let {
                        Geocoder(context, Locale.getDefault()).getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                    }

                    if (address != null) {
                        zipCode = address[0].postalCode
                        editTextLocation.text.clear()
                        editTextLocation.setText(address[0].locality)
                    }

                } catch (_: Exception) {
                    showError(R.string.errorMessage)
                }
            }

            override fun onFailed(e: String?) {
                super.onFailed(e)
                showError(R.string.errorMessage)
            }
        }
    }

}