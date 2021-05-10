package c0d3.vitreen.app.fragments.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_product.*
import kotlinx.android.synthetic.main.product_item.view.*
import java.util.*

class ProductFragment : VFragment(
    layoutId = R.layout.fragment_product,
    topIcon = R.drawable.bigicon_adding,
    hasOptionsMenu = true,
    topMenuId = R.menu.menu_product,
    requireAuth = true,
    loginNavigationId = R.id.action_navigation_product_to_navigation_login
) {

    // TODO : A VERIFIER !!!!!!!

    private var productId: String? = null
    private var counter = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productId = arguments?.getString(KEY_PRODUCT_ID).orEmpty()
        //Récupération du produit courant
        productId?.let {
            viewModel.getProduct(it)
                .observe(viewLifecycleOwner, { pair ->
                    if (handleError(pair.first, R.string.errorMessage)) return@observe
                    //Affichage des infos
                    textViewTitle.setText(pair.second.title)
                    textViewBrand.setText(pair.second.brand)
                    textViewDescription.setText(pair.second.description)
                    textViewPrice.setText(getString(R.string.price, pair.second.price))
                    textViewDimensions.setText(pair.second.size ?: "")
                    textViewCategory.setText(pair.second.category.name)
                    textViewLocation.setText("${pair.second.location.name}${if (pair.second.location.zipCode == null) "" else pair.second.location.zipCode}")
                    textViewBrand.setText(pair.second.brand)
                    textViewDimensions.setText(pair.second.size)

                    viewModel.getImages(pair.second.id, pair.second.nbImages)
                        .observe(viewLifecycleOwner, { imagesPair ->
                            if (handleError(imagesPair.first, R.string.errorMessage)) return@observe
                            imageViewProduct.setImageBitmap(imagesPair.second.get(counter))
                            buttonPreviousImage.setOnClickListener {
                                counter =
                                    if (counter-- <= 0) (imagesPair.second.size - 1) else counter--
                                imageViewProduct.setImageBitmap(imagesPair.second.get(counter))
                            }

                            buttonNextImage.setOnClickListener {
                                counter =
                                    if (counter++ >= (imagesPair.second.size - 1)) 0 else counter++
                                imageViewProduct.setImageBitmap(imagesPair.second.get(counter))
                            }
                        })
                })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                addRemoveToFavorites()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun addRemoveToFavorites() {
        viewModel.getUser(user!!)
            .observe(viewLifecycleOwner, { pair ->
                if (handleError(pair.first, R.string.errorMessage)) return@observe
                var listFavorite = pair.second.favoriteProductsId
                if (!listFavorite.contains(productId)) {
                    listFavorite.add(productId!!)
                } else {
                    listFavorite.remove(productId!!)
                }
                viewModel.updateUser(pair.second.id, favoriteProducts = listFavorite)
            })
    }

}