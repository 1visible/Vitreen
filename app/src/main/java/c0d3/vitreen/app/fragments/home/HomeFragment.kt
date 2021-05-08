package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Gravity.CENTER
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProducts().observe(viewLifecycleOwner, { product ->
            val products = product
        })

        // Show loading spinner
        setSpinnerVisibility(VISIBLE)
        setErrorView(GONE)

        if(user == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    if(isFragmentVisible){

                    }
                }
                .addOnFailureListener {
                    if(isFragmentVisible){
                        setSpinnerVisibility(GONE)
                        setErrorView(VISIBLE)
                        showMessage(R.string.anonymous_error)
                    }
                }
        }

        if (user == null) {
            auth.signInAnonymously()
            // errorView.visibility = View.GONE
        } else {
            if (user!!.isAnonymous) {
                navigateTo(R.id.action_navigation_home_to_navigation_error)
            } else {
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
                                        if(recyclerViewProducts != null) {
                                            recyclerViewProducts.visibility = View.GONE
                                        }
                                        if(errorView != null) {
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