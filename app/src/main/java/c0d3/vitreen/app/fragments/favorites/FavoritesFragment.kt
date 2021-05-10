package c0d3.vitreen.app.fragments.favorites

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.adapter.ProductAdapter
import c0d3.vitreen.app.models.dto.ProductDTO
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.recyclerViewProducts
import kotlinx.android.synthetic.main.fragment_profile.*

class FavoritesFragment : VFragment(
    R.layout.fragment_favorites,
    R.drawable.bigicon_favorites,
    -1,
    true,
    R.menu.menu_favorites,
    false
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewProducts.visibility = View.VISIBLE
        val productAdapter = ProductAdapter { product -> adapterOnClick(product) }
        recyclerViewProducts.adapter = productAdapter
        //Récupération de la liste d'annonces en favori de l'utilisateur courant
        viewModel.getUser(user!!)
            .observe(viewLifecycleOwner, { pair ->
                if (handleError(pair.first, R.string.errorMessage)) return@observe
                if (pair.second.favoriteProductsId.size == 0) {
                    recyclerViewProducts.visibility = View.GONE
                    return@observe
                }
                viewModel.getProducts(
                    limit = false,
                    ids = pair.second.favoriteProductsId
                ).observe(viewLifecycleOwner, { pair ->
                    if (handleError(pair.first, R.string.no_favorites)) {
                        return@observe
                    }
                    // Else if there is no products to display, hide loading spinner and show empty text
                    if (pair.second.isEmpty()) {
                        setSpinnerVisibility(View.GONE)
                        textViewNoProducts.visibility = View.VISIBLE
                        return@observe
                    }

                    // Else, show products in recycler view
                    val adapter = ProductAdapter { product -> adapterOnClick(product) }
                    adapter.submitList(pair.second.map { item -> item.toDTO() })
                    recyclerViewProducts.adapter = adapter
                    recyclerViewProducts.visibility = View.VISIBLE
                })
            })
    }

    // TODO : Ajouter les items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Put things here
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Opens Advert  when RecyclerView item is clicked. */
    private fun adapterOnClick(product: ProductDTO) {
        navigateTo(
            R.id.action_navigation_favorites_to_navigation_product,
            Constants.KEY_PRODUCT_ID to product.id
        )
    }
}