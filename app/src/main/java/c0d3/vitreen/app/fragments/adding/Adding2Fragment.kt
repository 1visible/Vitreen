package c0d3.vitreen.app.fragments.adding

import android.R.attr
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
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
    private var counter = 0

    private var imagesRef: StorageReference = storage.reference.child("images")

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
                nbImageMax = imagesCountMax

                buttonConfirmation.setOnClickListener {
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
            //Ouverture de la galerie afin de récupérer des images
            val intent = Intent(Intent.ACTION_PICK)
            //On autorise l'utilisation de plusieurs images unquement dans le cas où l'api est compatible
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
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
            //propriété qui va permettre de suivre les éléments lors de l'utilisation des prvious et next button
            counter = 0
            // Get the Image from data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            imagesEncodedList = ArrayList<String>()
            //Vérifie que l'utilsateur a bien récupéré des données
            if (data != null) {
                //Cas où l'utilisateur récupère qu'une seule image
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
                    //Reset du tableau
                    mArrayUri.clear()
                    mArrayUri.add(mImageUri)
                    cursor.close()
                    //Convertion des images en InputStream afin de pouvoir envoyer les images vers le serveur
                    mArrayUri.forEach {
                        mArrayInputStream.clear()
                        requireContext().contentResolver.openInputStream(it)?.let { it1 ->
                            mArrayInputStream.add(
                                it1
                            )
                        }
                    }
                    //Logique de la Card
                    imageViewProduct.setImageURI(mImageUri)
                    buttonPreviousImage.visibility = View.GONE
                    buttonNextImage.visibility = View.GONE
                    buttonRemoveImage.setOnClickListener {
                        mArrayUri.clear()
                        mArrayInputStream.clear()
                        context?.let { it ->
                            imageViewProduct.setImageDrawable(
                                ContextCompat.getDrawable(
                                    it,
                                    R.drawable.image_placeholder
                                )
                            )
                        }
                    }
                } else {
                    //L'utilisateur a sélectionné plusieurs images
                    if (data.getClipData() != null) {
                        //On rend visible les éléments de la cards qui sont nécessaires
                        buttonRemoveImage.visibility = View.VISIBLE
                        buttonNextImage.visibility = View.VISIBLE
                        buttonPreviousImage.visibility = View.VISIBLE
                        val mClipData: ClipData = data.getClipData()!!
                        //Reset de la tab
                        mArrayUri.clear()
                        //Parcours des images selectionnées
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
                        //Vérification des droits de sélections d'images de l'utilisateur
                        if (mArrayUri.size <= nbImageMax) {
                            //Reset du tab InputeStream
                            mArrayInputStream.clear()
                            //Conversion
                            mArrayUri.forEach {
                                requireContext().contentResolver?.openInputStream(
                                    it
                                )?.let { it1 ->
                                    mArrayInputStream.add(
                                        it1
                                    )
                                }

                            }
                            //Logique Card
                            imageViewProduct.setImageURI(mArrayUri.get(counter))
                            buttonPreviousImage.setOnClickListener {
                                counter = if (counter-- <= 0) (mArrayUri.size - 1) else counter--
                                imageViewProduct.setImageURI(mArrayUri.get(counter))
                            }
                            buttonNextImage.setOnClickListener {
                                counter = if (counter++ >= (mArrayUri.size - 1)) 0 else counter++
                                imageViewProduct.setImageURI(mArrayUri.get(counter))
                            }
                            buttonRemoveImage.setOnClickListener {
                                mArrayUri.remove(mArrayUri.get(counter))
                                mArrayInputStream.remove(mArrayInputStream.get(counter))
                                counter = if (counter < 0) 0 else counter - 1
                                if (mArrayUri.size > 0) {
                                    imageViewProduct.setImageURI(mArrayUri.get(counter))
                                } else {
                                    //Une fois qu'on a supprimé toutes les images
                                    //Disparition des boutons + Affichage du placeholder
                                    buttonRemoveImage.visibility = View.GONE
                                    buttonNextImage.visibility = View.GONE
                                    buttonPreviousImage.visibility = View.GONE
                                    context?.let { it ->
                                        imageViewProduct.setImageDrawable(
                                            ContextCompat.getDrawable(
                                                it,
                                                R.drawable.image_placeholder
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            context?.let {
                                Toast.makeText(
                                    it,
                                    "Le nombre d'image est trop élevé",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }
}