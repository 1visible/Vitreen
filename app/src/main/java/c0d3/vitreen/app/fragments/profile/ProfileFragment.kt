package c0d3.vitreen.app.fragments.profile

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.models.User
import c0d3.vitreen.app.models.dto.UserDTO
import c0d3.vitreen.app.models.dto.sdto.ProductSDTO
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.Constants.Companion.TAG
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.recyclerViewProducts


class ProfileFragment : VFragment(
        R.layout.fragment_profile,
        R.drawable.bigicon_profile,
        -1,
        true,
        R.menu.menu_profile,
        true,
        R.id.action_navigation_profile_to_navigation_login
) {

    private var userDTO: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show loading spinner and hide empty view
        setSpinnerVisibility(VISIBLE)
        setEmptyView(GONE)

        // If user is not signed in, skip this part
        if(!isUserSignedIn())
            return

        // Get current user informations
        viewModel.getUser(user!!).observeOnce(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val user = pair.second
            // If the call fails, show error message, hide loading spinner and show empty view
            if(handleError(errorCode, R.string.error_placeholder)) return@observeOnce

            // Else, fill the profile with user informations and store them
            showProducts(user.productsId)
            fillProfile(user)
            userDTO = user
        })

        // On delete button click, delete user account
        buttonDeleteAccount.setOnClickListener {
            userDTO?.let { user -> deleteAccount(user) }
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

    /* Opens product detail when RecyclerView item is clicked. */
    private fun adapterOnClick(product: Product) {
        navigateTo(R.id.action_navigation_profile_to_navigation_product, KEY_PRODUCT_ID to product.id)
    }

    private fun fillProfile(user: User) {
        // Fill personal informations
        textViewFullname.text = user.fullname
        textViewEmailAddress.text = user.emailAddress
        textViewPhoneNumber.text = user.phoneNumber
        textViewPostalAddress.text = getString(R.string.location_template, user.location.name, user.location.zipCode)

        // Remove checkmark on least prefered contact method
        if(user.contactByPhone)
            textViewEmailAddress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_envelope, 0, 0, 0)
        else
            textViewPhoneNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_phone, 0, 0, 0)

        // Show personal informations section
        textViewPersonalInformations.visibility = VISIBLE
        textViewFullname.visibility = VISIBLE
        textViewEmailAddress.visibility = VISIBLE
        textViewPhoneNumber.visibility = VISIBLE
        textViewPostalAddress.visibility = VISIBLE

        if(!user.isProfessional)
            return

        // Fill professional informations (and show section)
        textViewCompanyName.text = user.companyName
        textViewSiretNumber.text = user.siretNumber
        textViewProfessionalInformations.visibility = VISIBLE
        textViewCompanyName.visibility = VISIBLE
        textViewSiretNumber.visibility = VISIBLE
    }

    private fun deleteAccount(user: User) {

        // If user is not signed in, skip this part
        if(!isUserSignedIn())
            return

        

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

    private fun showProducts(ids: ArrayList<String>) {
        viewModel.getProducts(ids = ids).observe(viewLifecycleOwner, { pair ->
            val errorCode = pair.first
            val products = pair.second

            // Show "My products" title
            textViewMyProducts.visibility = VISIBLE

            // If the call fails, show error message, hide loading spinner and show empty text
            if (handleError(errorCode)) {
                textViewNoProducts.visibility = VISIBLE
                return@observe
            }

            // Else if there is no products to display, hide loading spinner and show empty text
            if (products.isEmpty()) {
                setSpinnerVisibility(GONE)
                textViewNoProducts.visibility = VISIBLE
                return@observe
            }

            // Else, show products in recycler view
            val adapter = ProductAdapter { product -> adapterOnClick(product) }
            recyclerViewProducts.adapter = adapter
            recyclerViewProducts.visibility = VISIBLE
        })
    }

}