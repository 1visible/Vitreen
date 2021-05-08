package c0d3.vitreen.app.fragments.favorites

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.dto.sdto.ProductSDTO
import c0d3.vitreen.app.utils.Constants
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
        val productAdapter = ProductAdapter { product -> adapterOnClick(product) }
        recyclerViewProducts.adapter = productAdapter
        //Récupération de la liste d'annonces en favori de l'utilisateur courant
        usersCollection
            .whereEqualTo("emailAddress", user!!.email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() == 1) {
                    var favoritesProductsIdsList: ArrayList<String>? = null
                    for (document in documents) {
                        favoritesProductsIdsList =
                            document.get("favoriteProductsId") as java.util.ArrayList<String>?
                    }
                    //Si la liste existe et possède des éléments
                    if (favoritesProductsIdsList != null) {
                        if (favoritesProductsIdsList.size > 0) {
                            var productsList: ArrayList<ProductSDTO> = ArrayList()
                            //Récupération des infos des annonces présentes dans cette liste
                            favoritesProductsIdsList.forEach {
                                productsCollection
                                    .document(it)
                                    .get()
                                    .addOnSuccessListener { product ->
                                        categoriesCollection
                                            .document(product.get("categoryId") as String)
                                            .get()
                                            .addOnSuccessListener { category ->
                                                locationsCollection
                                                    .document(product.get("locationId") as String)
                                                    .get()
                                                    .addOnSuccessListener { location ->
                                                        productsList.add(
                                                            ProductSDTO(
                                                                product.id,
                                                                product.get("title") as String,
                                                                category.get("name") as String,
                                                                location.get("name") as String,
                                                                product.get("price") as Double
                                                            )
                                                        )
                                                        //Au moment où nous avons récupérer toutes les infos des produits favoris
                                                        if (productsList.size == favoritesProductsIdsList.size) {
                                                            //On les passe à l'adapteur
                                                            productAdapter.submitList(productsList)
                                                        }
                                                    }
                                            }
                                    }
                            }
                        } else {
                            //Affichage du text "Aucun favori"
                            recyclerViewProducts.visibility = View.GONE
                        }
                    } else {
                        //Affichage du text "Aucun favori"
                        recyclerViewProducts.visibility = View.GONE
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
        navigateTo(
            R.id.action_navigation_favorites_to_navigation_product,
            Constants.KEY_PRODUCT_ID to product.id
        )
    }
}