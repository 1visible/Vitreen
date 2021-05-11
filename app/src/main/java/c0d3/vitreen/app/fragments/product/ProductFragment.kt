package c0d3.vitreen.app.fragments.product

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Discussion
import c0d3.vitreen.app.models.Message
import c0d3.vitreen.app.models.dto.ProductDTO
import c0d3.vitreen.app.utils.Constants.Companion.KEY_DISCUSSION_ID
import c0d3.vitreen.app.utils.Constants.Companion.KEY_PRODUCT_ID
import c0d3.vitreen.app.utils.ProductImageViewModel
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.product_item.view.*
import kotlinx.android.synthetic.main.fragment_product.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [ProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductFragment : VFragment(
    R.layout.fragment_product,
    R.drawable.bigicon_adding,
    -1,
    true,
    R.menu.menu_product,
    true,
    R.id.action_navigation_product_to_navigation_login
) {
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
                        .observe(viewLifecycleOwner, Images@{ imagesPair ->
                            if (handleError(imagesPair.first, R.string.errorMessage)) return@Images
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
                    buttonSendMessage.setOnClickListener { view ->
                        viewModel.getUser(user!!).observeOnce(viewLifecycleOwner, { pairUser ->
                            if (handleError(pairUser.first)) return@observeOnce
                            viewModel.getUser(id = pair.second.ownerId)
                                .observeOnce(viewLifecycleOwner, productOwner@{ productOwnerPair ->
                                    if (handleError(productOwnerPair.first)) return@productOwner
                                    var firstMessage = ArrayList<Message>()
                                    firstMessage.add(
                                        Message(
                                            pairUser.second.id,
                                            pairUser.second.fullname,
                                            productOwnerPair.second.fullname,
                                            getString(R.string.createDiscussion)
                                        )
                                    )
                                    val discussion = Discussion(
                                        pairUser.second.id,
                                        pair.second.id,
                                        pair.second.title,
                                        pair.second.ownerId,
                                        firstMessage
                                    )
                                    viewModel.addDiscussion(discussion)
                                        .observeOnce(viewLifecycleOwner, addDiscussion@{ pair ->
                                            if (handleError(pair.first)) return@addDiscussion
                                            navigateTo(
                                                R.id.action_navigation_product_to_navigation_discussion,
                                                KEY_DISCUSSION_ID to pair.second
                                            )
                                        })
                                })
                        })
                    }
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