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
import c0d3.vitreen.app.models.dto.AdvertDTO
import c0d3.vitreen.app.utils.Constants.Companion.KEYADVERTID
import c0d3.vitreen.app.utils.ProductImageViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.advert_item.view.*
import kotlinx.android.synthetic.main.fragment_advert.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [ProductFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductFragment : Fragment() {
    private var advertId: String? = null

    private val imagesListView: ProductImageViewModel by viewModels()
    private var imageList = ArrayList<Bitmap>()

    private val db = Firebase.firestore

    private val auth = Firebase.auth
    private val user = auth.currentUser

    private val adverts = db.collection("Adverts")
    private val userDb = db.collection("Users")

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            advertId = it.getString(KEYADVERTID)
        }
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
        advertId?.let {
            adverts
                .document(it)
                .get()
                .addOnSuccessListener { advert ->
                    val advertDTO = AdvertDTO(
                        advert.id,
                        advert.get("title") as String,
                        advert.get("description") as String,
                        advert.get("price") as Long,
                        advert.get("brand") as String,
                        advert.get("size") as String?,
                        advert.get("numberOfConsultations") as Long,
                        advert.get("reported") as ArrayList<String>?,
                        advert.get("locationId") as String,
                        advert.get("categoryId") as String,
                        advert.get("nbImages") as Long,
                        advert.get("ownerId") as String,
                        advert.get("createdAt") as String,
                        advert.get("modifiedAt") as String
                    )

                    advertTitle.text = advertDTO.title.toEditable()
                    advertBrand.text = advertDTO.brand.toEditable()
                    advertDescription.text = advertDTO.description.toEditable()
                    advertPrice.text = "${advertDTO.price}${getString(R.string.euros)}".toEditable()
                    advertSize.text = advertDTO.size?.toEditable()

                    for (i in 0..advertDTO.nbImages) {
                        val advertImageRef = storageRef.child("images/${advertDTO.id}/image_$i.png")
                        val ONE_MEGABYTE: Long = 1024 * 1024
                        advertImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                            imageList.add(
                                BitmapFactory.decodeByteArray(
                                    it,
                                    0,
                                    it.size
                                )
                            )
                            println("----------------------${imageList.size}")
                            if (imageList.size.toLong() == advertDTO.nbImages) {
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
                        userDb
                            .whereEqualTo("emailAddress", user.email)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.size() == 1) {
                                    for (document in documents) {
                                        println("-------------${advertDTO.id}")
                                        var listFavorite =
                                            document.get("favoriteAdvertsId") as ArrayList<String>?
                                        if (listFavorite != null) {
                                            if ((!listFavorite.contains(advertDTO.id))
                                            ) {
                                                listFavorite.add(advertDTO.id)
                                                Toast.makeText(
                                                    context,
                                                    getString(R.string.addFavorite),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                listFavorite.remove(advertDTO.id)
                                                Toast.makeText(
                                                    context,
                                                    getString(R.string.removeFavorite),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            userDb
                                                .document(document.id)
                                                .update("favoriteAdvertsId", listFavorite)
                                        }
                                    }
                                }
                            }
                    }
                }
        }
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    companion object {
        @JvmStatic
        fun newInstance(advertId: String) =
            ProductFragment().apply {
                arguments = Bundle().apply {
                    putString(KEYADVERTID, advertId)
                }
            }
    }
}