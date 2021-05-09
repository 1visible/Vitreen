package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.*
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.dto.CategoryDTO
import c0d3.vitreen.app.models.dto.LocationDTO
import c0d3.vitreen.app.models.dto.sdto.ProductSDTO
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.Constants.Companion.TAG
import c0d3.vitreen.app.utils.VFragment
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.empty_view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.fragment_profile.*

class HomeFragment : VFragment(
    R.layout.fragment_home,
    R.drawable.bigicon_logo,
    R.string.welcome,
    true,
    R.menu.menu_messages
) {

    private var locationId = ""
    private var userId = ""
    private var listProduct: ArrayList<ProductSDTO> = ArrayList()
    private var researchList: ArrayList<ProductSDTO> = ArrayList()

    private var researchFlag = false

    private var categoriesDTO = ArrayList<CategoryDTO>()
    private var locationDTO = ArrayList<LocationDTO>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show loading spinner and hide empty view
        setSpinnerVisibility(VISIBLE)
        setEmptyView(GONE)

        // If the user is signed out
        if(user == null) {
            // Try to sign in with anonymous account
            viewModel.signInAnonymously().observeOnce(viewLifecycleOwner, { errorCode ->
                // If the call fails, show error message, hide loading spinner and show empty view
                if(handleError(errorCode, R.string.no_products)) return@observeOnce

                // If the user is signed in anonymously, get products
                viewModel.getProducts().observe(viewLifecycleOwner, { pair ->
                    val errorCode2 = pair.first
                    val products = pair.second
                    // If the call fails, show error message, hide loading spinner and show empty view
                    if(handleError(errorCode2, R.string.no_products)) return@observe

                    Log.i(TAG, "Test $products")
                })
            })
        }



        if (user == null) {
            // auth.signInAnonymously()
            // errorView.visibility = View.GONE
        } else {
            if (user!!.isAnonymous) {
                navigateTo(R.id.action_navigation_home_to_navigation_error)
            } else {
                recyclerViewProducts.visibility = View.VISIBLE
                val productAdapter = ProductAdapter { product -> adapterOnClick(product) }
                recyclerViewProducts.adapter = productAdapter
                //Flag qui va éviter de faire cette requête lors d'un reload et affichera le résultat de la dernière recherche
                if (!researchFlag) {
                    usersCollection
                        .whereEqualTo("emailAddress", user!!.email)
                        .get()
                        .addOnSuccessListener {

                            if (it.documents.size == 1) {
                                for (document in it.documents) {
                                    locationId = document.get("locationId") as String
                                    userId = document.id
                                }
                                productsCollection
                                    .whereEqualTo("locationId", locationId)
                                    .whereNotEqualTo("ownerId", userId)
                                    .orderBy("ownerId")
                                    .orderBy("createdAt", Query.Direction.DESCENDING)
                                    .limit(Constants.IMAGES_LIMIT_HOME_PAGE.toLong())
                                    .get()
                                    .addOnSuccessListener {
                                        if (it.documents.size > 0) {
                                            for (document in it.documents) {
                                                categoriesCollection
                                                    .document(document.get("categoryId") as String)
                                                    .get()
                                                    .addOnSuccessListener { category ->
                                                        println("----------------------------")
                                                        println(category.get("name") as String)
                                                        println("----------------------------")
                                                        locationsCollection
                                                            .document(document.get("locationId") as String)
                                                            .get()
                                                            .addOnSuccessListener { location ->
                                                                println("----------------------------")
                                                                println(location.get("name") as String)
                                                                println("----------------------------")
                                                                listProduct.add(
                                                                    ProductSDTO(
                                                                        document.id,
                                                                        document.get("title") as String,
                                                                        category.get("name") as String,
                                                                        location.get("name") as String,
                                                                        document.get("price") as Double
                                                                    )
                                                                )
                                                                if (listProduct.size == it.documents.size) {
                                                                    println("-----------------------------")
                                                                    println(listProduct.size)
                                                                    println("-----------------------------")
                                                                    productAdapter.submitList(
                                                                        listProduct
                                                                    )
                                                                }
                                                            }
                                                    }
                                            }
                                        } else {
                                            if (recyclerViewProducts != null) {
                                                recyclerViewProducts.visibility = View.GONE
                                            }
                                            if(emptyView != null) {
                                                emptyView.visibility = View.VISIBLE
                                            }
                                            Toast.makeText(
                                                requireContext(),
                                                "docuement.size<0",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .addOnFailureListener(requireActivity()) {
                                        recyclerViewProducts.visibility = View.GONE
                                        emptyView.visibility = View.VISIBLE
                                        showMessage(R.string.errorMessage)
                                    }
                            }
                        }
                }

                //Récupération des catégories
                categoriesCollection.get()
                    .addOnSuccessListener {
                        it.forEach { category ->
                            categoriesDTO.add(
                                CategoryDTO(
                                    category.id,
                                    category.get("name") as String
                                )
                            )
                        }
                        // Ajout des catégories au menu déroulant du formulaire
                        val adapter = context?.let { context ->
                            ArrayAdapter(
                                context,
                                R.layout.dropdown_menu_item,
                                categoriesDTO.map { it.dtoToModel().name })
                        }
                        (textInputCategory?.editText as? AutoCompleteTextView)?.setAdapter(
                            adapter
                        )
                    }

                //Récupération des localisation
                locationsCollection.get()
                    .addOnSuccessListener {
                        it.forEach { location ->
                            locationDTO.add(
                                LocationDTO(
                                    location.id,
                                    location.get("name") as String,
                                    location.get("zipCode") as Long?
                                )
                            )
                        }

                        //Ajout de la liste au menu déroulant
                        var location =
                            ArrayList<String>(locationDTO.map { it.dtoToModel().name })
                        location.add(0, "Ma localisation")
                        val adapter = context?.let { context ->
                            ArrayAdapter(
                                context,
                                R.layout.dropdown_menu_item,
                                location
                            )
                        }
                        (autoCompleteLocation?.editText as? AutoCompleteTextView)?.setAdapter(
                            adapter
                        )
                    }

                buttonResearch.setOnClickListener {
                    //Si aucun champs n'est rempli alors on affiche la liste de produits venant de l'algo de base
                    if (isAllInputEmpty(
                            editTextResearchText,
                            editTextMaxPrice,
                            textInputCategory,
                            autoCompleteLocation,
                            editTextBrand
                        )
                    ) {
                        println("----------------------")
                        println("vide")
                        println("----------------------")
                        productAdapter.submitList(listProduct)
                        return@setOnClickListener
                    }
                    //Création d'une requête selon les champs remplis
                    var query = productsCollection as Query
                    if (editTextResearchText.editText?.text.toString() != "") {
                        println("----------------------")
                        println("title")
                        println("----------------------")
                        query = query.whereEqualTo(
                            "title",
                            editTextResearchText.editText?.text.toString()
                        )
                    }
                    if (editTextMaxPrice.editText?.text.toString() != "") {
                        println("----------------------")
                        println("price")
                        println("----------------------")
                        query = query.whereLessThanOrEqualTo(
                            "price",
                            editTextMaxPrice.editText?.text.toString().toLong()
                        )
                    }
                    if (textInputCategory?.editText?.text.toString() != "") {
                        println("----------------------")
                        println("category")
                        println("----------------------")
                        var categoryId = ""
                        categoriesDTO.forEach { categoryDTO ->
                            if (categoryDTO.name == textInputCategory?.editText?.text.toString()) {
                                categoryId = categoryDTO.id
                            }
                        }
                        query = query.whereEqualTo("categoryId", categoryId)
                    }

                    if (autoCompleteLocation.editText?.text.toString() != "") {
                        println("----------------------")
                        println("location")
                        println("----------------------")
                        var locationId = ""
                        locationDTO.forEach { locationDTO ->
                            if (locationDTO.name == autoCompleteLocation.editText?.text.toString()) {
                                locationId = locationDTO.id
                            }
                        }
                        query = query.whereEqualTo("locationId", locationId)
                    }

                    if (editTextBrand.editText?.text.toString() != "") {
                        println("----------------------")
                        println("brand")
                        println("----------------------")
                        query = query.whereEqualTo("brand", editTextBrand.editText?.text.toString())
                    }

                    query
                        .get()
                        .addOnSuccessListener {
                            if (it.documents.size > 0) {
                                println("----------------------")
                                println("query OK")
                                println("----------------------")
                                researchList.clear()
                                it.documents.forEach { product ->
                                    researchList.add(
                                        ProductSDTO(
                                            product.id,
                                            product.get("title") as String,
                                            "une catégorie",
                                            "une location",
                                            product.get("price") as Double
                                        )
                                    )
                                }
                                println("-----------------------")
                                println(researchList.get(0).title)
                                println("-----------------------")
                                researchFlag = true
                                //Ajout des résultats de la recherche dans le recyclerview
                                productAdapter.submitList(researchList)
                            } else {
                                println("----------------------")
                                println("0 item")
                                println("----------------------")
                                recyclerViewProducts.visibility = View.GONE
                                emptyView.visibility = View.VISIBLE
                            }

                        }
                }
            }
        }
    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // navigate to search screen
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Opens Product when RecyclerView item is clicked. */
    private fun adapterOnClick(product: ProductSDTO) { // TODO : Déplacement vers fragment annonce
        navigateTo(R.id.action_navigation_home_to_navigation_product, KEY_PRODUCT_ID to product.id)
    }

}