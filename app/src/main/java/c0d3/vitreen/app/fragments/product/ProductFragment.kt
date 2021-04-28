package c0d3.vitreen.app.fragments.product

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.dto.ProductDTO
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
    false,
    -1,
    true,
    R.id.action_navigation_product_to_navigation_login
) {
    private var productId: String? = null
    private var counter = 0
    private var imageList = ArrayList<Bitmap>()

    private val storageRef = storage.reference

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
            productsCollection
                .document(it)
                .get()
                .addOnSuccessListener { product ->
                    val productDTO = ProductDTO(
                        product.id,
                        product.get("title") as String,
                        product.get("description") as String,
                        product.get("price") as Long,
                        product.get("brand") as String,
                        product.get("size") as String?,
                        product.get("numberOfConsultations") as Long,
                        product.get("reported") as ArrayList<String>?,
                        product.get("locationId") as String,
                        product.get("categoryId") as String,
                        product.get("nbImages") as Long,
                        product.get("ownerId") as String,
                        product.get("createdAt") as String,
                        product.get("modifiedAt") as String
                    )
                    categoriesCollection
                        .document(productDTO.categoryId)
                        .get()
                        .addOnSuccessListener { category ->
                            locationsCollection
                                .document(productDTO.locationId)
                                .get()
                                .addOnSuccessListener { location ->
                                    val zipCode =
                                        if (location.get("zipCode") as Long? == null) "" else "(${
                                            location.get("zipCode") as Long?
                                        })"
                                    //Affichage des infos
                                    textViewTitle.setText(productDTO.title)
                                    textViewBrand.setText(productDTO.brand)
                                    textViewDescription.setText(productDTO.description)
                                    textViewPrice.setText(getString(R.string.price, productDTO.price))
                                    textViewDimensions.setText(productDTO.size ?: "")
                                    textViewCategory.setText(category.get("name") as String)
                                    textViewLocation.setText("${location.get("name") as String}${zipCode}")
                                    textViewBrand.setText(productDTO.brand)
                                    textViewDimensions.setText(productDTO.size)
                                }
                        }
                    //Téléchargement des images
                    for (i in 0..productDTO.nbImages-1) {
                        val productImageRef =
                            storageRef.child("images/${productDTO.id}/image_$i")
                        val ONE_MEGABYTE: Long = 1024 * 1024
                        //Téléchargement d'une image
                        productImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                            imageList.add(BitmapFactory.decodeByteArray(it, 0, it.size))
                            //Une fois que toutes les images téléchargées faire le traitement suivant
                            //Logique de la card
                            if (imageList.size.toLong() == productDTO.nbImages) {
                                imageViewProduct.setImageBitmap(imageList.get(counter))
                                buttonPreviousImage.setOnClickListener {
                                    counter = if (counter-- <= 0) (imageList.size - 1) else counter--
                                    imageViewProduct.setImageBitmap(imageList.get(counter))
                                }

                                buttonNextImage.setOnClickListener {
                                    counter = if (counter++ >= (imageList.size - 1)) 0 else counter++
                                    imageViewProduct.setImageBitmap(imageList.get(counter))
                                }
                            }
                        }.addOnFailureListener {
                            // Handle any errors
                        }
                    }
                    /*advertFavButton.setOnClickListener {
                        usersCollection
                            .whereEqualTo("emailAddress", user!!.email)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.size() == 1) {
                                    for (document in documents) {
                                        println("-------------${productDTO.id}")
                                        var listFavorite =
                                            document.get("favoriteProductsId") as ArrayList<String>?
                                        if (listFavorite != null) {
                                            if ((!listFavorite.contains(productDTO.id))
                                            ) {
                                                listFavorite.add(productDTO.id)
                                            } else {
                                                listFavorite.remove(productDTO.id)
                                            }
                                            usersCollection
                                                .document(document.id)
                                                .update("favoriteProductsId", listFavorite)
                                        }
                                    }
                                }
                            }
                    }*/
                }
        }
    }

}