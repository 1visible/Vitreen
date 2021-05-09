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
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.dto.CategoryDTO
import c0d3.vitreen.app.models.dto.LocationDTO
import c0d3.vitreen.app.models.dto.ProductDTO
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
                // Else show the products
                showProducts()
            })
        }
        // Else if the user is signed in anonymously
        else if(user!!.isAnonymous) {
            showProducts()
        }
        // Else (the user is signed in)
        else {
            viewModel.getUser(user!!).observeOnce(viewLifecycleOwner, { pair ->
                val errorCode2 = pair.first
                val user = pair.second
                // If the call fails, show error message, hide loading spinner and show empty view
                if(handleError(errorCode2, R.string.no_products)) return@observeOnce

                // Else, show the products according to user location
                showProducts(user.location)
            })
        }

        viewModel.getCategories().observeOnce(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val categories = pair.second
            // If the call fails, show error message, hide loading spinner and show empty view
            if(errorCode == -1) return@observeOnce

            // Else, set categories as editTextCategories choices if possible
            if(context != null) {
                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, categories.map { it.name })
                (textInputCategory?.editText as? AutoCompleteTextView)?.setAdapter(adapter)
            }
        })

        if (user == null) {
            // auth.signInAnonymously()
            // errorView.visibility = View.GONE
        } else {
            if (user!!.isAnonymous) {
                navigateTo(R.id.action_navigation_home_to_navigation_error)
            } else {
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
        // FIN
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
    private fun adapterOnClick(product: ProductDTO) { // TODO : Déplacement vers fragment annonce
        navigateTo(R.id.action_navigation_home_to_navigation_product, KEY_PRODUCT_ID to product.id)
    }

    private fun showProducts(location: Location? = null) {
        viewModel.getProducts(location = location).observe(viewLifecycleOwner, { pair ->
            val errorCode2 = pair.first
            val products = pair.second
            // If the call fails, show error message, hide loading spinner and show empty view
            if(handleError(errorCode2, R.string.no_products)) return@observe

            // Else if there is no products to display, hide loading spinner and show empty view
            if(products.isEmpty()) {
                setSpinnerVisibility(GONE)
                setEmptyView(VISIBLE, R.string.no_products)
                return@observe
            }

            // Else, show products in recycler view
            recyclerViewProducts.visibility = VISIBLE
            val adapter = ProductAdapter { product -> adapterOnClick(product) }
            recyclerViewProducts.adapter = adapter

        })
    }

}