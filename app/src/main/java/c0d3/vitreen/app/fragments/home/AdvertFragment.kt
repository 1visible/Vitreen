package c0d3.vitreen.app.fragments.home

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Advert
import c0d3.vitreen.app.models.dto.AdvertDTO
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.Constants.Companion.KEYADVERTID
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_advert.*

/**
 * A simple [Fragment] subclass.
 * Use the [AdvertFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdvertFragment : Fragment() {
    private var advertId: String? = null

    private val db = Firebase.firestore
    private val adverts = db.collection("Adverts")

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
                        advert.get("ownerId") as String,
                        advert.get("createdAt") as String,
                        advert.get("modifiedAt") as String
                    )

                    advertTitle.text = advertDTO.title.toEditable()
                    advertBrand.text = advertDTO.brand.toEditable()
                    advertDescription.text = advertDTO.description.toEditable()
                    advertPrice.text = "${advertDTO.price.toString()}€".toEditable()
                    advertSize.text = advertDTO.size?.toEditable()
                }
        }
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    companion object {
        @JvmStatic
        fun newInstance(advertId: String) =
            AdvertFragment().apply {
                arguments = Bundle().apply {
                    putString(KEYADVERTID, advertId)
                }
            }
    }
}