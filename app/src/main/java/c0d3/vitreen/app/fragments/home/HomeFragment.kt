package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.mini.ProductMini
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.VFragment
import com.google.firebase.firestore.Query
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
    private var listProduct: ArrayList<ProductMini> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (user == null) {
            auth.signInAnonymously()
            homeTextViewNoConnection.visibility = View.VISIBLE
            homeTextViewNPY.visibility = View.GONE
        } else {
            if (user!!.isAnonymous) {
                homeTextViewNoConnection.visibility = View.VISIBLE
                homeTextViewNPY.visibility = View.GONE
                homeRecyclerView.visibility = View.GONE
            } else {
                homeTextViewNoConnection.visibility = View.GONE
                homeTextViewNPY.visibility = View.GONE
                homeRecyclerView.visibility = View.VISIBLE
                val productAdapter = ProductAdapter { product -> adapterOnClick(product) }
                homeRecyclerView.adapter = productAdapter
                db.collection("Users")
                    .whereEqualTo("emailAddress", user!!.email)
                    .get()
                    .addOnSuccessListener {

                        if (it.documents.size == 1) {
                            for (document in it.documents) {
                                locationId = document.get("locationId") as String
                                userId = document.id
                            }
                            db.collection("Products")
                                .whereEqualTo("locationId", locationId)
                                .whereNotEqualTo("ownerId", userId)
                                .orderBy("ownerId")
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .limit(Constants.HomeLimit.toLong())
                                .get()
                                .addOnSuccessListener {
                                    if (it.documents.size > 0) {
                                        for (document in it.documents) {
                                            listProduct.add(
                                                ProductMini(
                                                    document.id,
                                                    document.get("title") as String,
                                                    document.get("description") as String,
                                                    document.get("price") as Long
                                                )
                                            )
                                        }

                                        productAdapter.submitList(listProduct)
                                    } else {
                                        homeRecyclerView.visibility = View.GONE
                                        homeTextViewNoConnection.visibility = View.GONE
                                        homeTextViewNPY.visibility = View.VISIBLE
                                        Toast.makeText(
                                            requireContext(),
                                            "docuement.size<0",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .addOnFailureListener(requireActivity()) {
                                    homeRecyclerView.visibility = View.GONE
                                    homeTextViewNoConnection.visibility = View.GONE
                                    homeTextViewNPY.visibility = View.VISIBLE
                                    Toast.makeText(
                                        context,
                                        "Une erreur s'est produite",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
    private fun adapterOnClick(product: ProductMini) { // TODO : Déplacement vers fragment annonce }

}