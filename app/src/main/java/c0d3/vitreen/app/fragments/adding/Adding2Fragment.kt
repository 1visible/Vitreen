package c0d3.vitreen.app.fragments.adding

import android.R.attr
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.models.dto.UserDTO
import c0d3.vitreen.app.utils.Constants.Companion.CATEGORY_ID
import c0d3.vitreen.app.utils.Constants.Companion.DESCRIPTION
import c0d3.vitreen.app.utils.Constants.Companion.GALLERY_REQUEST
import c0d3.vitreen.app.utils.Constants.Companion.GALLERY_REQUEST_TAG
import c0d3.vitreen.app.utils.Constants.Companion.IMAGES_LIMIT_PROFESSIONAL
import c0d3.vitreen.app.utils.Constants.Companion.IMAGES_LIMIT_USER
import c0d3.vitreen.app.utils.Constants.Companion.LOCATION_ID
import c0d3.vitreen.app.utils.Constants.Companion.PRICE
import c0d3.vitreen.app.utils.Constants.Companion.TITLE
import c0d3.vitreen.app.utils.VFragment
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.android.synthetic.main.fragment_adding1.*
import kotlinx.android.synthetic.main.fragment_adding2.*
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


class Adding2Fragment : VFragment(
    R.layout.fragment_adding2,
    R.drawable.bigicon_adding,
    -1,
    true,
    R.menu.menu_adding,
    true,
    R.id.action_navigation_adding2_to_navigation_home
) {
    private var imageEncoded: String? = null
    private var imagesEncodedList: ArrayList<String>? = null
    private var mArrayUri = ArrayList<Uri>()
    private var mArrayInputStream = ArrayList<InputStream>()
    private var nbImageMax = 0

    private var imagesRef: StorageReference = storage.reference.child("images")

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId: String = arguments?.getString(CATEGORY_ID).orEmpty()
        val title: String = arguments?.getString(TITLE).orEmpty()
        val price: String = arguments?.getString(PRICE).orEmpty()
        val locationId: String = arguments?.getString(LOCATION_ID).orEmpty()
        val description: String = arguments?.getString(DESCRIPTION).orEmpty()
        usersCollection
            .whereEqualTo("emailAddress", user?.email)
            .get()
            .addOnSuccessListener { documents ->

                if (documents.size() == 0) {
                    showError(R.string.errorMessage)
                    return@addOnSuccessListener
                }

                val document = documents.first()
                val user = UserDTO(document)
                val imagesCountMax =
                    if (user.isProfessional) IMAGES_LIMIT_PROFESSIONAL else IMAGES_LIMIT_USER

                buttonConfirmation.setOnClickListener {
                    println("-------------------------------------")
                    println("j'ai appuyé sur un bouton")
                    println("-------------------------------------")
                    // Vérifie que les champs du formulaire ne sont pas vides
                    if (isAnyInputEmpty(editTextBrand, editTextDimensions)) {
                        showError(R.string.errorMessage)
                        return@setOnClickListener
                    }

                    // Vérifie que les arguments récupérés ne sont pas vides
                    if (isAnyStringEmpty(categoryId, title, price, locationId, description)) {
                        showError(R.string.errorMessage)
                        return@setOnClickListener
                    }

                    val product = Product(
                        title = title,
                        description = description,
                        price = price.toLong(),
                        brand = editTextBrand.text.toString(),
                        size = editTextDimensions.text.toString(),
                        locationId = locationId,
                        categoryId = categoryId,
                        nbImages = mArrayInputStream.size.toLong(),
                        ownerId = user.id,
                        createdAt = Calendar.getInstance().time.toString(),
                        modifiedAt = ""
                    )

                    productsCollection
                        .add(product)
                        .addOnSuccessListener { product ->
                            val metadata = storageMetadata { contentType = "image/jpg" }

                            for (i in mArrayInputStream.indices)
                                imagesRef.child("${product.id}/image_$i")
                                    .putStream(mArrayInputStream[i], metadata)

                            mArrayUri.clear()
                            mArrayInputStream.clear()
                            user.productsId =
                                if (user.productsId == null) ArrayList() else user.productsId
                            user.productsId?.add(product.id)
                            usersCollection.document(user.id).update("productsId", user.productsId)
                            navigateTo(R.id.action_navigation_adding2_to_navigation_home)
                            // TODO : Naviguer vers fragment du produit + Ajouter les OnFailure + Vérifier ce code
                        }

                }

            }

        buttonAddImage.setOnClickListener {
            println("---------------------------------")
            println("j'ai appuyé sur le bouton ajout d'image")
            println("---------------------------------")
            val intent = Intent(Intent.ACTION_PICK)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.type = "image/*"
            startActivityForResult(
                intent,
                GALLERY_REQUEST
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && null != attr.data) {
            println("---------------------------------")
            println("je suis à la récupération des images")
            println("---------------------------------")
            // Get the Image from data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            imagesEncodedList = ArrayList<String>()
            if (data != null) {
                println("---------------------------------")
                println("data non null")
                println("---------------------------------")
                if (data.getData() != null) {
                    println("---------------------------------")
                    println("data.getData non null")
                    println("---------------------------------")
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
                    //countImage.text = "1/$nbImageMax"
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
                    println("---------------------------------")
                    println("getData null")
                    println("---------------------------------")
                    if (data.getClipData() != null) {
                        println("---------------------------------")
                        println("clip data non null")
                        println("---------------------------------")
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
                            //countImage.text = "${mArrayUri.size}/$nbImageMax"
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
                            //countImage.text = "0/$nbImageMax"
                        }
                    }
                }
            }
        }
    }
}