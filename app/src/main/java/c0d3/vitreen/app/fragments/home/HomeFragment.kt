package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.dto.CategoryDTO
import c0d3.vitreen.app.models.dto.LocationDTO
import c0d3.vitreen.app.models.dto.sdto.ProductSDTO
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.VFragment
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.error_view.*
import kotlinx.android.synthetic.main.fragment_home.*

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

    private var researchFlag = false

    private var categoriesDTO = ArrayList<CategoryDTO>()
    private var locationDTO = ArrayList<LocationDTO>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (user == null) {
            auth.signInAnonymously()
            // errorView.visibility = View.GONE
        } else {
            if (user!!.isAnonymous) {
                navigateTo(R.id.action_navigation_home_to_navigation_error)
            } else {
                include.visibility = View.GONE
                recyclerViewProducts.visibility = View.VISIBLE
                val productAdapter = ProductAdapter { product -> adapterOnClick(product) }
                recyclerViewProducts.adapter = productAdapter
                if(!researchFlag) {
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
                                            if (errorView != null) {
                                                errorView.visibility = View.VISIBLE
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
                                        errorView.visibility = View.VISIBLE
                                        showMessage(R.string.errorMessage)
                                    }
                            }
                        }
                }
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
                                categoriesDTO.map { it.DtoToModel().name })
                        }
                        (textInputCategory?.editText as? AutoCompleteTextView)?.setAdapter(
                            adapter
                        )
                    }

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
                        var location =
                            ArrayList<String>(locationDTO.map { it.DtoToModel().name })
                        location.add(0, "Ma localisation")
                        val adapter = context?.let { context ->
                            ArrayAdapter(
                                context,
                                R.layout.dropdown_menu_item,
                                location
                            )
                        }
                        autoCompleteLocation.setAdapter(adapter)
                    }
                buttonResearch.setOnClickListener {
                    if (editTextResearchText.text.toString() == "" && editTextMaxPrice.text.toString() == "" && textInputCategory?.editText?.text.toString() == "" && autoCompleteLocation.text.toString() == "") {
                        println("---------------------------")
                        println("un champs vide")
                        println("---------------------------")
                        return@setOnClickListener
                    }
                    var query = productsCollection as Query
                    if (editTextResearchText.text.toString() != "") {
                       query = query.whereEqualTo(
                           "title",
                           editTextResearchText.text.toString()
                       )
                    }
                    if (editTextMaxPrice.text.toString() != "") {
                        query = query.whereLessThanOrEqualTo(
                            "price",
                            editTextMaxPrice.text.toString().toLong()
                        )
                    }
                    if (textInputCategory?.editText?.text.toString() != "") {
                        var categoryId = ""
                        categoriesDTO.forEach { categoryDTO ->
                            if (categoryDTO.name == textInputCategory?.editText?.text.toString()) {
                                categoryId = categoryDTO.id
                            }
                        }
                        query = query.whereEqualTo("categoryId", categoryId)
                    }

                    if (autoCompleteLocation.text.toString() != "") {
                        var locationId = ""
                        locationDTO.forEach { locationDTO ->
                            if (locationDTO.name == autoCompleteLocation.text.toString()) {
                                locationId = locationDTO.id
                            }
                        }
                        query = query.whereEqualTo("locationId", locationId)
                    }

                    query
                        .get()
                        .addOnSuccessListener {
                            listProduct.clear()
                            if(it.documents.size > 0) {
                                println("------------------")
                                println("j'ai trouvé des produits")
                                println("------------------")
                                it.documents.forEach { product ->
                                    listProduct.add(
                                        ProductSDTO(
                                            product.id,
                                            product.get("title") as String,
                                            "une catégorie",
                                            "une location",
                                            product.get("price") as Double
                                        )
                                    )
                                }
                                researchFlag = true
                                val adapter = ProductAdapter{product->adapterOnClick(product)}
                                recyclerViewProducts.adapter = adapter
                                adapter.submitList(listProduct)
                            }else{
                                println("-----------------------------")
                                println("0 produit trouvé")
                                println("-----------------------------")
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