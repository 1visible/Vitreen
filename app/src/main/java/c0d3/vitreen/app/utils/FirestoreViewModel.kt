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
import c0d3.vitreen.app.utils.Constants.Companion.REPORT_THRESHOLD
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class FirestoreViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    var exceptionLiveData = MutableLiveData<Int>()
    private var userLiveData = MutableLiveData<Pair<Int, User>>()
    var productsLiveData = MutableLiveData<Pair<Int, List<Product>>>()
    var categoriesLiveData = MutableLiveData<Pair<Int, List<Category>>>()
    var locationsLiveData = MutableLiveData<Pair<Int, List<Location>>>()
    var discussionsLiveData = MutableLiveData<Pair<Int, List<Discussion>>>()
    private var privateExceptionLiveData = MutableLiveData<Pair<Int, List<Bitmap>>>()
    private var privateBooleanLiveData = MutableLiveData<Boolean>()

    fun getProducts(
        owner: LifecycleOwner,
        limit: Boolean,
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

            privateBooleanLiveData.observeOnce(owner, {
                productsLiveData.value = exception to products
            })

            if(documents == null || documents.isEmpty)
                privateBooleanLiveData.value = true
            else
                documents.forEach { document ->
                    val product = toObject(document, Product::class.java)

                    if(product.reporters.size < REPORT_THRESHOLD) {
                        if(product.imagesPaths.isEmpty()) {
                            productTaskCounter++
                            products.add(product)

                            if(productTaskCounter == documents.size())
                                privateBooleanLiveData.value = true

                            return@forEach
                        }

                        getImages(product.imagesPaths.first()).observeOnce(owner, { pair ->
                            productTaskCounter++
                            val images = pair.second

                            if (images.isNotEmpty())
                                product.images = images

                            products.add(product)

                            if(productTaskCounter == documents.size())
                                privateBooleanLiveData.value = true
                        })
                    } else {
                        productTaskCounter++

                        if(productTaskCounter == documents.size())
                            privateBooleanLiveData.value = true
                    }
                }
        }

        return productsLiveData
    }

    fun getProduct(id: String, owner: LifecycleOwner): MutableLiveData<Pair<Int, List<Product>>> {
        repository.getProduct(id).addSnapshotListener { document, error ->
            var exception = if (error == null) -1 else R.string.NetworkException
            var product = document?.let { doc -> toObject(doc, Product::class.java) }

            if(product == null) {
                product = Product()

                if(exception == -1)
                    exception = R.string.ProductNotFoundException
            }

            if(product.imagesPaths.isNotEmpty())
                getImages(*product.imagesPaths.toTypedArray()).observeOnce(owner, { pair ->
                    val images = pair.second

                    if (images.isNotEmpty())
                        product.images = images

                    productsLiveData.value = exception to mutableListOf(product)
                })
            else
                productsLiveData.value = exception to mutableListOf(product)
        }

        return productsLiveData
    }

    fun signInAnonymously(): LiveData<Int> {
        return request(repository.signInAnonymously())
    }

    fun signIn(email: String, password: String): LiveData<Int> {
        return request(repository.signIn(email, password))
    }

    fun getUser(user: FirebaseUser): LiveData<Pair<Int, User>> {
        repository.getUser(user).addSnapshotListener { documents, error ->
            var exception = if (error == null) -1 else R.string.NetworkException
            var userData = User()

            if (documents != null && !documents.isEmpty)
                userData = toObject(documents.first(), User::class.java)
            else if(exception == -1)
                exception = R.string.NotFoundException

            userLiveData.value = exception to userData
        }

        return userLiveData
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

    fun getLocation(city: String): Location? {
        return locationsLiveData.value?.second?.first { location -> location.city == city }
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

    fun addConsultation(productId: String, consultation: Consultation): LiveData<Int> {
        return request(repository.addConsultation(productId, consultation), false)
    }

    fun addToFavorites(id: String, favoriteId: String): LiveData<Int> {
        return request(repository.addToFavorites(id, favoriteId))
    }

    fun removeFromFavorites(id: String, favoriteId: String): LiveData<Int> {
        return request(repository.removeFromFavorites(id, favoriteId))
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
        repository.getProducts(limit = false, ownerId = ownerId).get().addOnCompleteListener { task ->
            var exception = if (task.isSuccessful) -1 else R.string.ProductNotDeletedException
            val products = task.result

            if(products == null || products.isEmpty)
                exceptionLiveData.value = exception
            else
                products.let { docs ->
                    val ids = docs.map { doc -> doc.id }.toTypedArray()

                    docs.forEach { document ->
                        val product = toObject(document, Product::class.java)

                        deleteImages(product.imagesPaths)
                    }

                    repository.deleteProducts(*ids).addOnCompleteListener { task2 ->
                        val exception2 = if (task2.isSuccessful) -1 else R.string.ProductNotDeletedException

                        if(exception == -1 && exception2 != -1)
                            exception = exception2

                        exceptionLiveData.value = exception
                    }
                }
        }

        return exceptionLiveData
    }

    private fun deleteImages(paths: ArrayList<String>) {
        paths.forEach { path ->
            repository.deleteImage(path)
        }
    }

    fun deleteUser(user: FirebaseUser): LiveData<Int> {
        return request(repository.deleteUser(user))
    }

    // TODO      vvv Vérifier refactor vvv

    /*

    fun getDiscussions(
        userId: String? = null,
        productOwner: String? = null
    ): LiveData<Pair<Int, List<Discussion>>> {
        return getList(repository.getDiscussions(userId, productOwner), discussionsLiveData)
    }

    fun getDiscussion(discussionId: String): LiveData<Pair<Int, Discussion>> {
        repository.getDiscussion(discussionId).addSnapshotListener { discussion, exception ->
            var errorCode = if (exception == null) -1 else R.string.network_error
            val discussionData: Discussion

            if (discussion == null || !discussion.exists()) {
                discussionData = Discussion()
                if (exception == null)
                    errorCode = R.string.error_404
            } else
                discussionData =
                    toObject(discussion as QueryDocumentSnapshot, Discussion::class.java)

            discussionLiveData.value = Pair(errorCode, discussionData)
        }
        return discussionLiveData
    }

    fun updateDiscussion(id: String, messages: ArrayList<Message>) {
        repository.updateDiscussion(id, messages)
    }

    fun addDiscussion(discussion: Discussion): LiveData<Pair<Int, String>> {
        repository.addDiscussion(discussion).addOnCompleteListener { task ->
            val discussionData = task.result
            val errorCode =
                if (task.isSuccessful && discussionData != null) -1 else R.string.network_error
            if (discussionData != null) {
                DiscussionLiveData.value = Pair(errorCode, discussionData.id)
            }
        }
        return DiscussionLiveData
    }

     */

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