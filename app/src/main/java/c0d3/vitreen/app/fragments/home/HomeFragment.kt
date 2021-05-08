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
                recyclerViewProducts.visibility = View.GONE
                linearLayoutResearch.visibility = View.VISIBLE
                if (linearLayoutResearch.visibility == View.VISIBLE) {
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
                            (textInputLocation?.editText as? AutoCompleteTextView)?.setAdapter(
                                adapter
                            )
                        }

                }
                // errorView.visibility = View.GONE
                recyclerViewProducts.visibility = View.VISIBLE
                val productAdapter = ProductAdapter { product -> adapterOnClick(product) }
                recyclerViewProducts.adapter = productAdapter
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