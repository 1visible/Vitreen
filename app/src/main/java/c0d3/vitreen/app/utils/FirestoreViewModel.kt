package c0d3.vitreen.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.models.*
import c0d3.vitreen.app.utils.Constants.Companion.IMAGES_LIMIT_PROFESSIONAL
import c0d3.vitreen.app.utils.Constants.Companion.REPORT_THRESHOLD
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class FirestoreViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    var exceptionLiveData = MutableLiveData<Int>()
    var userLiveData = MutableLiveData<Pair<Int, User>>()
    var productsLiveData = MutableLiveData<Pair<Int, List<Product>>>()
    var categoriesLiveData = MutableLiveData<Pair<Int, List<Category>>>()
    var locationsLiveData = MutableLiveData<Pair<Int, List<Location>>>()
    private var privateExceptionLiveData = MutableLiveData<Pair<Int, List<Bitmap>>>()
    private var privateExceptionLiveData2 = MutableLiveData<Int>()

    fun getProducts(
        owner: LifecycleOwner,
        limit: Boolean = true,
        title: String? = null,
        price: Double? = null,
        brand: String? = null,
        location: Location? = null,
        category: Category? = null,
        ownerId: String? = null
    ): LiveData<Pair<Int, List<Product>>> {
        val query = repository.getProducts(limit, title, price, brand, location, category, ownerId)

        query.addSnapshotListener { documents, error ->
            val exception = if (error == null) -1 else R.string.NetworkException
            val products = mutableListOf<Product>()
            var productTaskCounter = 0

            documents?.forEach { document ->
                val product = toObject(document, Product::class.java)

                if(product.reporters.size < REPORT_THRESHOLD) {
                    if(product.imagesPaths.isEmpty()) {
                        productTaskCounter++
                        products.add(product)

                        if(productTaskCounter == documents.size())
                            privateExceptionLiveData2.value = exception

                        return@forEach
                    }

                    getImages(product.imagesPaths.first()).observeOnce(owner, { pair ->
                        productTaskCounter++
                        val exception2 = pair.first
                        val images = pair.second

                        if (exception2 == -1 && images.isNotEmpty())
                            product.images = images

                        products.add(product)

                        if(productTaskCounter == documents.size())
                            privateExceptionLiveData2.value = exception
                    })
                } else {
                    productTaskCounter++

                    if(productTaskCounter == documents.size())
                        privateExceptionLiveData2.value = exception
                }
            }
        }

        return productsLiveData
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

    fun getImages(vararg paths: String): MutableLiveData<Pair<Int, List<Bitmap>>> {
        val images = mutableListOf<Bitmap>()
        var imageTaskCounter = 0
        var exception = -1

        if(paths.isEmpty())
            privateExceptionLiveData.value = -1 to images
        else
            paths.forEach { path ->
                repository.getImage(path).addOnCompleteListener { task ->
                    imageTaskCounter++
                    val bytes = task.result

                    if(!task.isSuccessful || bytes == null)
                        exception = R.string.ImageNotFoundException
                    else {
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        images.add(bitmap)
                    }

                    if(imageTaskCounter == paths.size)
                        privateExceptionLiveData.value = exception to images
                }
            }

        return privateExceptionLiveData
    }

    fun updateLocation(locationId: String, zipCode: Long): LiveData<Int> {
        return request(repository.updateLocation(locationId, zipCode), false)
    }

    fun updateProduct(productId: String, consultation: Consultation): LiveData<Int> {
        return request(repository.updateProduct(productId, consultation), false)
    }

    fun updateUser(id: String, favoriteId: String): LiveData<Int> {
        return request(repository.updateUser(id, favoriteId))
    }

    fun addLocation(location: Location): LiveData<Int> {
        return request(repository.addLocation(location))
    }

    fun addProduct(product: Product, inputStreamList: ArrayList<InputStream>, owner: LifecycleOwner): MutableLiveData<Pair<Int, List<Product>>> {
        val folder = UUID.randomUUID().toString()
        val imagesPaths = ArrayList<String>()
        var exception = -1
        var imageTaskCounter = 0

        privateExceptionLiveData.observeOnce(owner, {
            product.imagesPaths = imagesPaths

            repository.addProduct(product).addOnCompleteListener { task ->
                val productData = task.result
                var exception2 = if (task.isSuccessful && productData != null) -1 else R.string.NetworkException

                if(productData != null)
                    product.id = productData.id

                if(exception2 == -1 && exception != -1)
                    exception2 = exception

                productsLiveData.value = exception2 to mutableListOf(product)
            }
        })

        if(inputStreamList.isEmpty())
            privateExceptionLiveData.value = -1 to mutableListOf()
        else
            inputStreamList.forEach { inputStream ->
                val path = "${folder}/${UUID.randomUUID()}"
                repository.addImage(path, inputStream).addOnCompleteListener { task ->
                    imageTaskCounter++

                    if(!task.isSuccessful)
                        exception = R.string.ImageNotAddedException
                    else
                        imagesPaths.add(path)

                    if(imageTaskCounter == inputStreamList.size)
                        privateExceptionLiveData.value = exception to mutableListOf()
                }
            }

        return productsLiveData
    }

    fun deleteProducts(ownerId: String): LiveData<Int> {
        deleteImages(ids)
        return request(repository.deleteProducts(ownerId))
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
        query.addSnapshotListener { documents, error ->
            val exception = if (error == null) -1 else R.string.NetworkException
            val values: MutableList<T> = mutableListOf()

            documents?.forEach { document ->
                val value = toObject(document, T::class.java)
                values.add(value)
            }

            liveData.value = exception to values
        }

        return liveData
    }

    private fun <T> request(request: Task<T>, updateLiveData: Boolean = true): LiveData<Int> {
        request.addOnCompleteListener { task ->
            val exception = if (task.isSuccessful) -1 else R.string.NetworkException

            if(updateLiveData)
                exceptionLiveData.value = exception
        }

        return exceptionLiveData
    }

    private inline fun <reified T: Entity> requestWith404(request: Task<QuerySnapshot>, liveData: MutableLiveData<Pair<Int, T>>): LiveData<Pair<Int, T>> {
        request.addOnCompleteListener { task ->
            var exception = if (task.isSuccessful) -1 else R.string.network_error
            val documents = task.result
            val value: T

            if(documents != null && !documents.isEmpty) {
                value = toObject(documents.first(), T::class.java)
            } else {
                value = T::class.java.newInstance()
                if(task.isSuccessful)
                    exception = R.string.error_404
            }

            liveData.value = exception to value
        }

        return liveData
    }

    private inline fun <reified T: Entity> requestWith404D(request: Task<DocumentSnapshot>, liveData: MutableLiveData<Pair<Int, T>>): LiveData<Pair<Int, T>> {
        request.addOnCompleteListener { task ->
            var exception = if (task.isSuccessful) -1 else R.string.network_error
            val document = task.result
            val value: T

            val obj = document?.let { doc -> toObject(doc, T::class.java) }

            if(obj != null)
                value = obj
            else {
                value = T::class.java.newInstance()
                if(task.isSuccessful)
                    exception = R.string.error_404
            }

            liveData.value = exception to value
        }

        return liveData
    }

    private inline fun <reified T : Entity> toObject(document: DocumentSnapshot, @NonNull type: Class<T>): T {
        val value: T
        val obj = document.toObject(type)

        if(obj == null)
            value = T::class.java.newInstance()
        else {
            obj.id = document.id
            value = obj
        }

        return value
    }
}