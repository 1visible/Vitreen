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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
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
    private var imagesLiveData: MutableLiveData<Pair<Int, List<Bitmap>>> = MutableLiveData()


    fun getProducts(
        limit: Boolean = true,
        title: String? = null,
        price: Double? = null,
        brand: String? = null,
        location: Location? = null,
        category: Category? = null,
        ids: ArrayList<String>? = null
    ): LiveData<Pair<Int, List<Product>>> {
        return requestList(repository.getProducts(limit, title, price, brand, location, category,ids), productsLiveData)
    }

    fun getProduct(id: String): LiveData<Pair<Int, Product>> {
        return requestWith404D(repository.getProduct(id), productLiveData)
    }

    fun signInAnonymously(): LiveData<Int> {
        return request(repository.signInAnonymously())
    }

    fun signIn(email: String, password: String): LiveData<Int> {
        return request(repository.signIn(email, password))
    }

    fun getUser(user: FirebaseUser): LiveData<Pair<Int, User>> {
        return requestWith404(repository.getUser(user), userLiveData)
    }

    fun linkUser(user: FirebaseUser, email: String, password: String): LiveData<Int> {
        return request(repository.linkUser(user, email, password))
    }

    fun registerUser(email: String, password: String): LiveData<Int> {
        return request(repository.registerUser(email, password))
    }

    fun addUser(user: User): LiveData<Int> {
        return request(repository.addUser(user))
    }

    fun getCategories(): LiveData<Pair<Int, List<Category>>> {
        return requestList(repository.getCategories(), categoriesLiveData)
    }

    fun getLocations(): LiveData<Pair<Int, List<Location>>> {
        return requestList(repository.getLocations(), locationsLiveData)
    }

    fun getLocation(city: String): LiveData<Pair<Int, Location>> {
        return requestWith404(repository.getLocation(city), locationLiveData)
    }

    fun getImages(productId: String, nbImagesL: Long): MutableLiveData<Pair<Int, List<Bitmap>>> {
        val images: ArrayList<Bitmap> = ArrayList()
        val nbImages = nbImagesL.toInt()
        var errorCode = -1

        for (number in 0 until nbImages) {
            repository.getImage(productId, number).addOnCompleteListener { task ->
                val documents = task.result

                if(!task.isSuccessful || documents == null)
                    errorCode = R.string.network_error

                if(documents != null) {
                    val bitmap = BitmapFactory.decodeByteArray(documents, 0, documents.size)
                    images.add(bitmap)
                }

                if (images.size == nbImages)
                    imagesLiveData.value = errorCode to images
            }
        }

        return imagesLiveData
    }

    fun updateLocation(locationId: String, zipCode: Long): LiveData<Int> {
        return request(repository.updateLocation(locationId, zipCode), false)
    }

    @Throws(NullPointerException::class)
    fun updateUser(id: String, productsIds: ArrayList<String>? = null, favoritesIds: ArrayList<String>? = null): LiveData<Int> {
        val request = repository.updateUser(id, productsIds, favoritesIds)

        if(request != null)
            return request(request)
        else
            throw NullPointerException()
    }

    fun addLocation(location: Location): LiveData<Int> {
        return request(repository.addLocation(location))
    }

    private fun addImages(productId: String, inputStream: ArrayList<InputStream>) {
        repository.addImages(productId, inputStream)
    }

    fun addProduct(product: Product, inputStream: ArrayList<InputStream>, user: User): LiveData<Int> {
        repository.addProduct(product).addOnCompleteListener { task ->
            val productData = task.result
            val errorCode = if (task.isSuccessful && productData != null) -1 else R.string.network_error

            if(productData != null) {
                product.id = productData.id
                addImages(product.id, inputStream)
                user.productsIds.add(product.id)
                updateUser(user.id, user.productsIds)
            }

            errorCodeLiveData.value = errorCode
        }

        return errorCodeLiveData
    }

    fun deleteProducts(ids: ArrayList<String>): LiveData<Int> {
        deleteImages(ids)
        return request(repository.deleteProducts(ids))
    }

    private fun deleteImages(ids: ArrayList<String>) {
        ids.forEach { id ->
            for (number in 0..IMAGES_LIMIT_PROFESSIONAL)
                repository.deleteImage(id, number)
        }
    }

    fun deleteUser(user: FirebaseUser): LiveData<Int> {
        return request(repository.deleteUser(user))
    }

    private inline fun <reified T : Entity> requestList(query: Query, liveData: MutableLiveData<Pair<Int, List<T>>>): LiveData<Pair<Int, List<T>>> {
        query.addSnapshotListener { documents, exception ->
            val errorCode = if (exception == null) -1 else R.string.network_error
            val values: MutableList<T> = mutableListOf()

            if (documents != null)
                for (document in documents) {
                    val value = toObject(document, T::class.java)
                    values.add(value)
                }

            liveData.value = errorCode to values
        }

        return liveData
    }

    private fun <T> request(request: Task<T>, updateLiveData: Boolean = true): LiveData<Int> {
        request.addOnCompleteListener { task ->
            val errorCode = if (task.isSuccessful) -1 else R.string.network_error

            if(updateLiveData)
                errorCodeLiveData.value = errorCode
        }

        return errorCodeLiveData
    }

    private inline fun <reified T: Entity> requestWith404(request: Task<QuerySnapshot>, liveData: MutableLiveData<Pair<Int, T>>): LiveData<Pair<Int, T>> {
        request.addOnCompleteListener { task ->
            var errorCode = if (task.isSuccessful) -1 else R.string.network_error
            val documents = task.result
            val value: T

            if(documents != null && !documents.isEmpty)
                value = toObject(documents.first(), T::class.java)
            else {
                value = T::class.java.newInstance()
                if(task.isSuccessful)
                    errorCode = R.string.error_404
            }


            liveData.value = errorCode to value
        }

        return liveData
    }

    private inline fun <reified T: Entity> requestWith404D(request: Task<DocumentSnapshot>, liveData: MutableLiveData<Pair<Int, T>>): LiveData<Pair<Int, T>> {
        request.addOnCompleteListener { task ->
            var errorCode = if (task.isSuccessful) -1 else R.string.network_error
            val document = task.result
            val value: T

            if(document != null)
                value = toObject(document as QueryDocumentSnapshot, T::class.java)
            else {
                value = T::class.java.newInstance()
                if(task.isSuccessful)
                    errorCode = R.string.error_404
            }

            liveData.value = errorCode to value
        }

        return liveData
    }

    private fun <T : Entity> toObject(document: QueryDocumentSnapshot, @NonNull type: Class<T>): T {
        val obj = document.toObject(type)
        obj.id = document.id

        return obj
    }
}