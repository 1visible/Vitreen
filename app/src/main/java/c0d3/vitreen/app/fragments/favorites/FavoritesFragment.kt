package c0d3.vitreen.app.fragments.favorites

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.dto.sdto.ProductSDTO
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_favorites.*

class FavoritesFragment : VFragment(
    R.layout.fragment_favorites,
    R.drawable.bigicon_favorites,
    -1,
    true,
    R.menu.menu_favorites,
    true,
    R.id.action_navigation_favorites_to_navigation_error
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewProducts.visibility = View.VISIBLE
        textViewNoFavorites.visibility = View.GONE
        //Récupération de la liste d'annonces en favori de l'utilisateur courant
        usersCollection
            .whereEqualTo("emailAddress", user!!.email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() == 1) {
                    var favoritesProductsIdsList: ArrayList<String>? = null
                    for (document in documents) {
                        favoritesProductsIdsList =
                            document.get("favoriteAdvertsId") as java.util.ArrayList<String>?
                    }
                    //Si la liste existe et possède des éléments
                    if (favoritesProductsIdsList != null) {
                        if (favoritesProductsIdsList.size > 0) {
                            var productsList: ArrayList<ProductSDTO> = ArrayList()
                            val productAdapter: ProductAdapter =
                                ProductAdapter { product -> adapterOnClick(product) }
                            recyclerViewProducts.adapter = productAdapter
                            //Récupération des infos des annonces présentes dans cette liste
                            favoritesProductsIdsList.forEach {
                                productsCollection
                                    .document(it)
                                    .get()
                                    .addOnSuccessListener { product ->
                                        productsList.add(
                                            ProductSDTO(
                                                product.id,
                                                product.get("title") as String,
                                                product.get("description") as String,
                                                product.get("price") as Long
                                            )
                                        )
                                        if (productsList.size == favoritesProductsIdsList.size) {
                                            productAdapter.submitList(productsList)
                                        }
                                    }
                            }
                        } else {
                            //Affichage du text "Aucun favori"
                            recyclerViewProducts.visibility = View.GONE
                            textViewNoFavorites.visibility = View.VISIBLE
                        }
                    } else {
                        //Affichage du text "Aucun favori"
                        recyclerViewProducts.visibility = View.GONE
                        textViewNoFavorites.visibility = View.VISIBLE
                    }
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

    /* Opens Advert  when RecyclerView item is clicked. */
    private fun adapterOnClick(product: ProductSDTO) {

    }
}