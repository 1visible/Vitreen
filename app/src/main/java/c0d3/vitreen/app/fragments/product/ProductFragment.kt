package c0d3.vitreen.app.fragments.product

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.dto.ProductDTO
import c0d3.vitreen.app.utils.Constants.Companion.KEYADVERTID
import c0d3.vitreen.app.utils.ProductImageViewModel
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.product_item.view.*
import kotlinx.android.synthetic.main.fragment_advert.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [ProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductFragment : VFragment(
    R.layout.fragment_advert,
    R.drawable.bigicon_adding,
    -1,
    false,
    -1,
    true,
    R.id.action_navigation_product_to_navigation_login
) {
    private var productId: String? = null

    private val imagesListView: ProductImageViewModel by viewModels()
    private var imageList = ArrayList<Bitmap>()

    private val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_advert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productId = arguments?.getString(KEYADVERTID).orEmpty()
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

                    advertTitle.text = productDTO.title.toEditable()
                    advertBrand.text = productDTO.brand.toEditable()
                    advertDescription.text = productDTO.description.toEditable()
                    advertPrice.text = "${productDTO.price}".toEditable()
                    advertSize.text = productDTO.size?.toEditable()

                    for (i in 0..productDTO.nbImages) {
                        val productImageRef = storageRef.child("images/${productDTO.id}/image_$i.png")
                        val ONE_MEGABYTE: Long = 1024 * 1024
                        productImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                            imageList.add(
                                BitmapFactory.decodeByteArray(
                                    it,
                                    0,
                                    it.size
                                )
                            )
                            println("----------------------${imageList.size}")
                            if (imageList.size.toLong() == productDTO.nbImages) {
                                imagesListView.advertImages.value = imageList
                                println(imagesListView.advertImages.value?.size)
                            }
                        }.addOnFailureListener {
                            // Handle any errors
                        }
                    }
                    val observer = Observer<ArrayList<Bitmap>> { newList ->
                        newList.forEach { image ->
                            var imageView = ImageView(requireContext())
                            imageView.setImageBitmap(image)
                            ImageLayout.addView(imageView)
                        }
                    }
                    imagesListView.advertImages.observe(viewLifecycleOwner, observer)
                    advertFavButton.setOnClickListener {
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
                    }
                }
        }
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

}