package c0d3.vitreen.app.fragments.adding

import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentTransaction
import c0d3.vitreen.app.R
import c0d3.vitreen.app.fragments.home.HomeFragment
import c0d3.vitreen.app.models.Advert
import c0d3.vitreen.app.models.dto.UserDTO
import c0d3.vitreen.app.utils.ChildFragment
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.utils.Constants.Companion.GALLERY_REQUEST
import c0d3.vitreen.app.utils.Constants.Companion.PERSO_LIMIT_IMAGES
import c0d3.vitreen.app.utils.Constants.Companion.PRO_LIMIT_IMAGES
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.android.synthetic.main.fragment_adding2.*
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Adding2Fragment : ChildFragment() {
    private var categoryId: String = ""
    private var title: String = ""
    private var price: String = ""
    private var locationId: String = ""
    private var description: String = ""

    private var imageEncoded: String? = null
    private var imagesEncodedList: ArrayList<String>? = null
    private var mArrayUri = ArrayList<Uri>()
    private var mArrayInputStream = ArrayList<InputStream>()
    private var nbImageMax = 0

    private var storage = Firebase.storage
    private var storageRef = storage.reference
    private var imagesRef: StorageReference? = storageRef.child("images")

    private val user = Firebase.auth.currentUser
    private val db = Firebase.firestore
    private val adverts = db.collection("Adverts")
    private val users = db.collection("Users")
    private val locations = db.collection("locations")

    override fun onAttach(context: Context) {
        super.onAttach(context)

        arguments?.getString(Constants.KEYADDADVERTS[0])?.let {
            categoryId = it
        }
        arguments?.getString(Constants.KEYADDADVERTS[1])?.let {
            title = it
        }
        arguments?.getString(Constants.KEYADDADVERTS[2])?.let {
            price = it
        }
        arguments?.getString(Constants.KEYADDADVERTS[3])?.let {
            locationId = it
        }
        arguments?.getString(Constants.KEYADDADVERTS[4])?.let {
            description = it
        }

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_adding2, container, false)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        users
                .whereEqualTo("email", user!!.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.size() == 1) {
                        var userDTO: UserDTO? = null
                        for (document in documents) {
                            userDTO = UserDTO(
                                    document.id,
                                    document.get("fullname") as String,
                                    document.get("emailAddress") as String,
                                    document.get("phoneNumber") as String,
                                    document.get("contactByPhone") as Boolean,
                                    document.get("isProfessional") as Boolean,
                                    document.get("locationId") as String,
                                    document.get("companyName") as String?,
                                    document.get("siretNumber") as String?,
                                    document.get("advertsId") as ArrayList<String>?,
                                    document.get("favoriteAdversId") as ArrayList<String>?,

                                    )
                        }
                        if (userDTO != null) {
                            nbImageMax = if (userDTO.isProfessional) PRO_LIMIT_IMAGES else PERSO_LIMIT_IMAGES
                        }
                        countImage.text = "0/${nbImageMax}"
                        imageButton.setOnClickListener {
                            println("----------------------------------Bouton image appuyé")
                            pickImages()
                        }
                        addButton.setOnClickListener {
                            if ((!(editTextSize.text.toString().replace("\\s", "")
                                            .equals(""))) && (!(editTextBrand.text.toString().replace("\\s", "")
                                            .equals("")) && (mArrayInputStream.size <= nbImageMax))
                            ) {
                                locations
                                        .document(locationId)
                                        .get()
                                        .addOnSuccessListener { location ->
                                            adverts.add(
                                                    Advert(
                                                            title = title,
                                                            description = description,
                                                            price = price.toFloat(),
                                                            brand = editTextBrand.text.toString(),
                                                            size = editTextSize.text.toString(),
                                                            locationId = locationId,
                                                            categoryId = categoryId,
                                                            ownerId = userDTO!!.id,
                                                            createdAt = Calendar.getInstance().time.toString("dd/MM/yyyy HH:mm:ss"),
                                                            modifiedAt = ""
                                                    )
                                            )
                                                    .addOnSuccessListener { advert ->
                                                        var i = 0
                                                        val metadata = storageMetadata {
                                                            contentType = "image/jpg"
                                                        }
                                                        mArrayInputStream.forEach {
                                                            imagesRef!!
                                                                    .child("${advert.id}/image_$i")
                                                                    .putStream(it, metadata)
                                                            i += 1
                                                        }
                                                        mArrayUri.clear()
                                                        mArrayInputStream.clear()
                                                        userDTO.advertsId =
                                                                if (userDTO.advertsId == null) ArrayList<String>() else userDTO.advertsId
                                                        userDTO.advertsId!!.add(advert.id)
                                                        users.document(userDTO.id)
                                                                .update("advertsId", userDTO.advertsId)
                                                        parentFragmentManager
                                                                .beginTransaction()
                                                                .replace(
                                                                        R.id.nav_host_fragment,
                                                                        HomeFragment.newInstance()
                                                                )
                                                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                                                .commit()
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(
                                                                requireContext(),
                                                                getString(R.string.ErrorMessage),
                                                                Toast.LENGTH_SHORT
                                                        )
                                                                .show()
                                                    }

                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                    requireContext(),
                                                    getString(R.string.ErrorMessage),
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                        }
                            } else {
                                Toast.makeText(
                                        requireContext(),
                                        getString(R.string.emptyFieldsAndImage),
                                        Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun pickImages() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                GALLERY_REQUEST
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            // When an Image is picked
            if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && null != attr.data) {
                // Get the Image from data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                imagesEncodedList = ArrayList<String>()
                if (data != null) {
                    if (data.getData() != null) {
                        val mImageUri: Uri = data.getData()!!

                        // Get the cursor
                        val cursor: Cursor = requireContext().contentResolver.query(
                                mImageUri,
                                filePathColumn, null, null, null
                        )!!
                        // Move to first row
                        cursor.moveToFirst()
                        val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                        imageEncoded = cursor.getString(columnIndex)
                        countImage.text = "1/$nbImageMax"
                        mArrayUri.clear()
                        mArrayUri.add(mImageUri)
                        cursor.close()
                        mArrayUri.forEach {
                            mArrayInputStream.clear()
                            requireContext().contentResolver.openInputStream(it)?.let { it1 ->
                                mArrayInputStream.add(
                                        it1
                                )
                            }
                        }
                    } else {
                        if (data.getClipData() != null) {
                            val mClipData: ClipData = data.getClipData()!!
                            mArrayUri.clear()
                            for (i in 0 until mClipData.itemCount) {
                                val item = mClipData.getItemAt(i)
                                val uri = item.uri
                                mArrayUri.add(uri)
                                // Get the cursor
                                val cursor: Cursor =
                                        requireContext().contentResolver.query(
                                                uri,
                                                filePathColumn,
                                                null,
                                                null,
                                                null
                                        )!!
                                // Move to first row
                                cursor.moveToFirst()
                                val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                                imageEncoded = cursor.getString(columnIndex)
                                imagesEncodedList!!.add(imageEncoded.toString())
                                cursor.close()
                            }
                            Log.v("LOG_TAG", "Selected Images " + mArrayUri.size)
                            if (mArrayUri.size <= nbImageMax) {
                                countImage.text = "${mArrayUri.size}/$nbImageMax"
                                mArrayInputStream.clear()
                                mArrayUri.forEach {
                                    requireContext().contentResolver?.openInputStream(
                                            it
                                    )?.let { it1 ->
                                        mArrayInputStream.add(
                                                it1
                                        )
                                    }

                                }
                            } else {
                                countImage.text = "0/$nbImageMax"
                                Toast.makeText(
                                        context,
                                        getString(R.string.tooMuchImages),
                                        Toast.LENGTH_SHORT
                                )
                                        .show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                        context, "You haven't picked Image",
                        Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG)
                    .show()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        @JvmStatic
        fun newInstance(
                categoryId: String,
                title: String,
                price: String,
                locationId: String,
                description: String
        ) = Adding2Fragment().apply {
            arguments = Bundle().apply {
                putString(Constants.KEYADDADVERTS[0], categoryId)
                putString(Constants.KEYADDADVERTS[1], title)
                putString(Constants.KEYADDADVERTS[2], price)
                putString(Constants.KEYADDADVERTS[3], locationId)
                putString(Constants.KEYADDADVERTS[4], description)
            }
        }
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

}