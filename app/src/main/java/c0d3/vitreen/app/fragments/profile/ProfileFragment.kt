package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.dto.UserDTO
import c0d3.vitreen.app.models.dto.sdto.ProductSDTO
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : VFragment(
    R.layout.fragment_profile,
    R.drawable.bigicon_profile,
    -1,
    true,
    R.menu.menu_profile,
    true,
    R.id.action_navigation_profile_to_navigation_login
) {

    private var productsList = ArrayList<ProductSDTO>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        usersCollection
            .whereEqualTo("emailAddress", user!!.email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() == 1) {
                    var userDTO: UserDTO? = null
                    for (document in documents) {
                        userDTO = UserDTO(
                            document.id,
                            document.get("fullname") as String,
                            document.get("emailAddress") as String,
                            document.get("phoneNumber") as String,
                            document.get("contactByPhone") as Boolean,
                            document.get("isProfessional") as Boolean,
                            document.get("locationId") as String,
                            document.get("companyName") as String?,
                            document.get("siretNumber") as String?,
                            document.get("productsId") as ArrayList<String>?,
                            document.get("favoriteProductsId") as java.util.ArrayList<String>?
                        )
                    }
                    if (userDTO != null) {
                        println("-------------------${userDTO.locationId}")
                        locationsCollection
                            .document(userDTO.locationId)
                            .get()
                            .addOnSuccessListener {
                                textViewFullname.text = userDTO.fullname
                                textViewEmailAddress.text = userDTO.emailAddress
                                textViewPhoneNumber.text = userDTO.phoneNumber
                                if (userDTO.contactByPhone) {
                                    textViewPhoneNumber.setCompoundDrawablesWithIntrinsicBounds(
                                        context?.let { it ->
                                            ContextCompat.getDrawable(it, R.drawable.icon_phone)
                                        }, null,
                                        context?.let { it1 ->
                                            ContextCompat.getDrawable(
                                                it1,
                                                R.drawable.icon_checkmark
                                            )
                                        }, null
                                    )
                                    textViewEmailAddress.setCompoundDrawablesWithIntrinsicBounds(
                                        context?.let { it ->
                                            ContextCompat.getDrawable(it, R.drawable.icon_envelope)
                                        },
                                        null,
                                        null,
                                        null
                                    )
                                } else {
                                    textViewEmailAddress.setCompoundDrawablesWithIntrinsicBounds(
                                        context?.let { it ->
                                            ContextCompat.getDrawable(it, R.drawable.icon_envelope)
                                        }, null,
                                        context?.let { it1 ->
                                            ContextCompat.getDrawable(
                                                it1,
                                                R.drawable.icon_checkmark
                                            )
                                        }, null
                                    )
                                    textViewPhoneNumber.setCompoundDrawablesWithIntrinsicBounds(
                                        context?.let { it ->
                                            ContextCompat.getDrawable(it, R.drawable.icon_phone)
                                        },
                                        null,
                                        null,
                                        null
                                    )
                                }
                                textViewPostalAddress.text =
                                    "${it.get("name") as String}(${it.get("zipCode") as Long?})"
                                if (userDTO.isProfessional) {
                                    textViewCompanyName.visibility = View.VISIBLE
                                    textViewCompanyName.text = userDTO.companyName
                                    textViewSiretNumber.visibility = View.VISIBLE
                                    textViewSiretNumber.text = userDTO.siretNumber
                                    //profilStatsButton.visibility = View.VISIBLE
                                } else {
                                    textViewCompanyName.visibility = View.GONE
                                    textViewSiretNumber.visibility = View.GONE
                                    // profilStatsButton.visibility = View.GONE
                                }
                                if ((userDTO.productsId != null) && (userDTO.productsId!!.size > 0)) {
                                    recyclerViewProducts.visibility = View.VISIBLE
                                    textViewNoProducts.visibility = View.GONE
                                    val productAdapter: ProductAdapter =
                                        ProductAdapter { product -> adapterOnClick(product) }
                                    recyclerViewProducts.adapter = productAdapter
                                    userDTO.productsId!!.forEach { productId ->
                                        println("------------------------------------")
                                        println("je rend le recyclerView visible ${productId}")
                                        println("------------------------------------")
                                        productsCollection
                                            .document(productId)
                                            .get()
                                            .addOnSuccessListener {
                                                categoriesCollection
                                                    .document(it.get("categoryId") as String)
                                                    .get()
                                                    .addOnSuccessListener { category ->
                                                        locationsCollection
                                                            .document(it.get("locationId") as String)
                                                            .get()
                                                            .addOnSuccessListener { location ->
                                                                productsList.add(
                                                                    ProductSDTO(
                                                                        it.id,
                                                                        it.get("title") as String,
                                                                        category.get("name") as String,
                                                                        location.get("name") as String,
                                                                        it.get("price") as Long
                                                                    )
                                                                )
                                                                if (productsList.size == userDTO.productsId!!.size) {
                                                                    productAdapter.submitList(
                                                                        productsList
                                                                    )
                                                                }
                                                            }
                                                    }

                                            }
                                    }
                                } else {
                                    recyclerViewProducts.visibility = View.GONE
                                    textViewNoProducts.visibility = View.VISIBLE
                                }
                            }
                    }

                } else {
                    println("--------------------------------documents size > 1")
                }
            }
            .addOnFailureListener {
                println("-------------------------problème")
            }

        signOutButton.setOnClickListener {
            auth.signOut()
            navigateTo(R.id.action_navigation_profile_to_navigation_home)
        }

    }

    /* Opens Advert  when RecyclerView item is clicked. */
    private fun adapterOnClick(product: ProductSDTO) {

    }

    //Supprimer un compte
    private fun deleteAccount() {
        if ((user != null) && (!user!!.isAnonymous)) {
            usersCollection
                .whereEqualTo("emailAddress", user!!.email)
                .get()
                .addOnSuccessListener { dbusers ->
                    if (dbusers.size() == 1) {
                        for (dbuser in dbusers) {
                            usersCollection.document(dbuser.id).delete()
                            user!!.delete()
                        }
                    }
                }
                .addOnFailureListener {
                    showError(R.string.errorMessage)
                }

        }
    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

}