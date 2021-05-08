package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.dto.UserDTO
import c0d3.vitreen.app.models.dto.sdto.ProductSDTO
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.Constants.Companion.TAG
import c0d3.vitreen.app.utils.VFragment
import com.google.android.gms.tasks.Tasks
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
    private var productsIdsList = ArrayList<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        viewModel.getProducts().observe(viewLifecycleOwner, { products ->
            Log.i(TAG, "Test $products")
        })
        */

        viewModel.signInAnonymously().observeOnce(viewLifecycleOwner, {
            textViewFullname.text
            Log.i(TAG, "Test $it")
        })

        if(user == null)
            return

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
                                val zipCodeString =
                                    if (it.get("zipCode") as Long? != null) "(${it.get("zipCode") as Long?})" else ""
                                textViewPostalAddress.text =
                                    it.get("name") as String + zipCodeString
                                if (userDTO.isProfessional) {
                                    textViewPersonalInformation.visibility = View.VISIBLE
                                    textViewCompanyName.visibility = View.VISIBLE
                                    textViewCompanyName.text = userDTO.companyName
                                    textViewSiretNumber.visibility = View.VISIBLE
                                    textViewSiretNumber.text = userDTO.siretNumber
                                    //profilStatsButton.visibility = View.VISIBLE
                                } else {
                                    textViewPersonalInformation.visibility = View.GONE
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
                                                                                it.get("price") as Double
                                                                        )
                                                                )
                                                                println("--------------------------")
                                                                println(it.get("price") as Double)
                                                                println("--------------------------")
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

        buttonDeleteAccount.setOnClickListener {
            deleteAccount()
        }

    }

    /* Opens Advert  when RecyclerView item is clicked. */
    private fun adapterOnClick(product: ProductSDTO) {
        navigateTo(
                R.id.action_navigation_profile_to_navigation_product,
                Constants.KEY_PRODUCT_ID to product.id
        )
    }

    //Supprimer un compte
    private fun deleteAccount() {
        //Vérification que l'utilisateur est connecté et non anonyme
        if ((user != null) && (!user!!.isAnonymous)) {
            //Recherche des infos de l'utilisateur courant
            usersCollection
                .whereEqualTo("emailAddress", user!!.email)
                .get()
                .addOnSuccessListener { dbusers ->
                    //Vérification que l'utilisateur est bien unique
                    if (dbusers.size() == 1) {
                        for (dbuser in dbusers) {
                            val currentUserId = dbuser.id
                            //Suppression des données de l'utilisateur
                            usersCollection.document(dbuser.id).delete()
                            //Suppression de tout les produits déposé par cet utilisateur
                            productsCollection
                                .whereEqualTo("ownerId", currentUserId)
                                .get()
                                .addOnSuccessListener { products ->
                                    for (product in products.documents) {
                                        for (i in 0..((product.get("nbImages") as Long) - 1)) {
                                            val image =
                                                storage.reference.child("images/${product.id}/image_$i")
                                            image.delete()
                                                .addOnSuccessListener {
                                                    if (i == ((product.get("nbImages") as Long) - 1)) {
                                                        //Suppression des infos de connexion de l'utilisateur
                                                        user!!.delete()
                                                        auth.signOut()
                                                    }
                                                }
                                                .addOnFailureListener {
                                                }
                                        }
                                        productsIdsList.add(product.id)
                                        productsCollection
                                            .document(product.id)
                                            .delete()
                                    }
                                    //Parcours de tout les utilisateurs
                                    //Retirer des favoris l'ensemble des produits effaçés
                                    usersCollection
                                        .get()
                                        .addOnSuccessListener { users ->
                                            for (user in users) {
                                                val favorites =
                                                    user.get("favoriteProductsId") as ArrayList<String>?
                                                if (favorites != null) {
                                                    for (productId in productsIdsList) {
                                                        favorites.remove(productId)
                                                    }
                                                    //Mise à jour de la liste de favoris
                                                    usersCollection
                                                        .document(user.id)
                                                        .update("favoriteProductsId", favorites)
                                                }
                                            }
                                        }
                                }.addOnSuccessListener {
                                    //Suppression des infos de connexion de l'utilisateur
                                    user!!.delete()
                                    auth.signOut()
                                }
                            navigateTo(R.id.action_navigation_profile_to_navigation_login)
                        }
                    }
                }
                .addOnFailureListener {
                    showMessage(R.string.errorMessage)
                }

        }
    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                navigateTo(R.id.action_navigation_profile_to_navigation_home)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}