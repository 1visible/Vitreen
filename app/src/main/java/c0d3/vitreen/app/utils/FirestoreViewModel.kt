package c0d3.vitreen.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.*
import c0d3.vitreen.app.utils.Constants.Companion.IMAGES_LIMIT_PROFESSIONAL
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.InputStream

class FirestoreViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private var errorCodeLiveData: MutableLiveData<Int> = MutableLiveData()
    private var productsLiveData: MutableLiveData<Pair<Int, List<Product>>> = MutableLiveData()
    private var productLiveData: MutableLiveData<Pair<Int, Product>> = MutableLiveData()
    private var userLiveData: MutableLiveData<Pair<Int, User>> = MutableLiveData()
    private var locationLiveData: MutableLiveData<Pair<Int, Location>> = MutableLiveData()
    var categoriesLiveData: MutableLiveData<Pair<Int, List<Category>>> = MutableLiveData()
    var locationsLiveData: MutableLiveData<Pair<Int, List<Location>>> = MutableLiveData()
    var imagesLiveData: MutableLiveData<Pair<Int, List<Bitmap>>> = MutableLiveData()


    fun getProducts(
        limit: Boolean = true,
        title: String? = null,
        price: Double? = null,
        brand: String? = null,
        location: Location? = null,
        category: Category? = null,
        ids: ArrayList<String>? = null
    ): LiveData<Pair<Int, List<Product>>> {
        return getList(
            repository.getProducts(limit, title, price, brand, location, category,ids),
            productsLiveData
        )
    }

    fun getProduct(id: String): LiveData<Pair<Int, Product>> {
        repository.getProduct(id)
            .addSnapshotListener { product, exception ->
                var errorCode = if (exception == null) -1 else R.string.network_error
                val productData: Product

                if (product == null) {
                    errorCode = R.string.errorMessage // TODO : Remplacer par un meilleur message
                    productData = Product()
                } else
                    productData = toObject(product as QueryDocumentSnapshot, Product::class.java)

                productLiveData.value = Pair(errorCode, productData)
            }
        return productLiveData
    }

    fun signInAnonymously(): LiveData<Int> {
        repository.signInAnonymously().addOnCompleteListener { task ->
            val errorCode = if (task.isSuccessful) -1 else R.string.network_error
            errorCodeLiveData.value = errorCode
        }

        return errorCodeLiveData
    }

    fun signIn(email: String, password: String): LiveData<Int> {
        repository.signIn(email, password).addOnCompleteListener { task ->
            val errorCode = if (task.isSuccessful) -1 else R.string.network_error
            errorCodeLiveData.value = errorCode
        }

        return errorCodeLiveData
    }

    fun getUser(user: FirebaseUser): LiveData<Pair<Int, User>> {
        repository.getUser(user).addSnapshotListener { users, exception ->
            var errorCode = if (exception == null) -1 else R.string.network_error
            val userData: User

            if (users == null || users.isEmpty) {
                errorCode = R.string.errorMessage // TODO : Remplacer par un meilleur message
                userData = User()
            } else {
                userData = toObject(users.first(), User::class.java)
            }

            userLiveData.value = Pair(errorCode, userData)
        }

        return userLiveData
    }

    fun getCategories(): LiveData<Pair<Int, List<Category>>> {
        return getList(repository.getCategories(), categoriesLiveData)
    }

    fun getLocations(): LiveData<Pair<Int, List<Location>>> {
        return getList(repository.getLocations(), locationsLiveData)
    }

    fun getLocation(name: String?): LiveData<Pair<Int, Location>> {
        repository.getLocations(name).addSnapshotListener { locations, exception ->
            var errorCode = if (exception == null) -1 else R.string.network_error
            val locationData: Location

            if (locations == null || locations.isEmpty) {
                errorCode = R.string.errorMessage // TODO : Remplacer par un meilleur message
                locationData = Location()
            } else
                locationData = toObject(locations.first(), Location::class.java)

            locationLiveData.value = Pair(errorCode, locationData)
        }

        return locationLiveData
    }

    fun getImages(productId: String, nbImages: Long): MutableLiveData<Pair<Int, List<Bitmap>>> {
        var imageList: ArrayList<Bitmap> = ArrayList()
        for (i in 0..nbImages - 1) {
            repository.getImages(productId, i)
                .addOnCompleteListener { task ->
                    var errorCode = if (task.isSuccessful == null) -1 else R.string.network_error
                    if (task.result == null) {
                        errorCode =
                            R.string.errorMessage // TODO : Remplacer par un meilleur message
                    } else {
                        imageList.add(
                            BitmapFactory.decodeByteArray(
                                task.result,
                                0,
                                task.result!!.size
                            )
                        )
                    }
                    if (imageList.size.toLong() == nbImages) {
                        imagesLiveData.value = Pair(errorCode, imageList)
                    }
                }
        }
        return imagesLiveData
    }

    fun updateLocation(locationId: String, zipCode: Long) {
        repository.updateLocation(locationId, zipCode)
    }

    fun updateUser(
        userId: String,
        productsId: ArrayList<String>? = null,
        favoriteProducts: ArrayList<String>? = null
    ) {
        if (productsId != null) repository.updateUser(userId, productsId)
        if (favoriteProducts != null) repository.updateUser(
            userId,
            favoritesProduct = favoriteProducts
        )
    }

    fun addLocation(location: Location) {
        repository.addLocation(location)
    }

    fun addImages(productId: String, inputStream: ArrayList<InputStream>) {
        repository.addImages(productId, inputStream)
    }

    fun addProduct(
        product: Product,
        inputStream: ArrayList<InputStream>,
        user: User
    ): LiveData<Int> {
        repository.addProduct(product)
            .addOnCompleteListener { task ->
                val errorCode = if (task.isSuccessful) -1 else R.string.network_error
                errorCodeLiveData.value = errorCode
                addImages(product.id, inputStream)
                user.productsId.add(product.id)
                updateUser(user.id, user.productsId)
            }
        return errorCodeLiveData
    }

    fun deleteProducts(ids: ArrayList<String>): LiveData<Int> {
        deleteImages(ids)
        repository.deleteProducts(ids).addOnCompleteListener { task ->
            val errorCode = if (task.isSuccessful) -1 else R.string.network_error
            errorCodeLiveData.value = errorCode
        }

        return errorCodeLiveData
    }

    private fun deleteImages(ids: ArrayList<String>) {
        ids.forEach { id ->
            for (number in 0..IMAGES_LIMIT_PROFESSIONAL)
                repository.deleteImage(id, number)
        }
    }

    fun deleteUser(user: FirebaseUser): LiveData<Int> {
        repository.deleteUser(user).addOnCompleteListener { task ->
            val errorCode = if (task.isSuccessful) -1 else R.string.network_error
            errorCodeLiveData.value = errorCode
        }

        return errorCodeLiveData
    }

    private inline fun <reified T : Entity> getList(
        query: Query,
        liveData: MutableLiveData<Pair<Int, List<T>>>
    ): LiveData<Pair<Int, List<T>>> {
        query.addSnapshotListener { documents, exception ->
            val errorCode = if (exception == null) -1 else R.string.network_error
            val valuesList: MutableList<T> = mutableListOf()

            if (documents != null) {
                for (document in documents) {
                    val value = toObject(document, T::class.java)
                    valuesList.add(value)
                }
            }

            liveData.value = Pair(errorCode, valuesList)
        }

        return liveData
    }

    private fun <T : Entity> toObject(document: QueryDocumentSnapshot, @NonNull type: Class<T>): T {
        val obj = document.toObject(type)
        obj.id = document.id

        return obj
    }
}