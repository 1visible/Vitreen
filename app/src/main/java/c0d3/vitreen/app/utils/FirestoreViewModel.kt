package c0d3.vitreen.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Menu
import androidx.annotation.NonNull
import androidx.lifecycle.*
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.*
import c0d3.vitreen.app.utils.Constants.Companion.REPORT_THRESHOLD
import c0d3.vitreen.app.utils.Constants.Companion.VTAG
import com.firebase.ui.auth.FirebaseAuthAnonymousUpgradeException
import com.firebase.ui.auth.util.FirebaseAuthError
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.firestore.*
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import java.io.InputStream
import java.util.*

class FirestoreViewModel(val state: SavedStateHandle) : ViewModel() {
    private val repository = FirestoreRepository()

    var isUserSignedIn: Boolean = false
    var product: Product = Product()
    var searchQuery: SearchQuery = SearchQuery()
    val user: MutableLiveData<Pair<Int, User>> = state.getLiveData("user")
    val categories: MutableLiveData<Pair<Int, List<Category>>> = state.getLiveData("categories")
    val locations: MutableLiveData<Pair<Int, List<Location>>> = state.getLiveData("locations")
    val discussions: MutableLiveData<Pair<Int, List<Discussion>>> =
        state.getLiveData("discussions") // TODO changer en Transformations sur user

    // var discussionsLiveData = MutableLiveData<Pair<Int, List<Discussion>>>()

    init {
        getCategories()
        getLocations()
    }

    fun getMenu(): MutableLiveData<Menu> {
        return MutableLiveData()
    }

    private fun isUserAvailable(): Boolean {
        user.value?.let { (exception, user) ->
            if (exception == -1 && user.emailAddress.isNotEmpty())
                return true
        }

        return false
    }

    fun setUserState(isUserSignedIn: Boolean, email: String? = null) {
        this.isUserSignedIn = isUserSignedIn

        when (isUserSignedIn) {
            false -> user.value = R.string.SignedOutException to User()
            true -> if (!isUserAvailable()) email?.let { mail -> getUser(mail) }
        }
    }

    fun getProducts(search: SearchQuery): MutableLiveData<ProductsContainer> {
        val productsContainer = MutableLiveData<ProductsContainer>()
        val query = repository.getProducts(search)

        query.addSnapshotListener { value, error ->
            var exception = if (error == null) -1 else R.string.NetworkException
            val list = arrayListOf<Pair<Product, Bitmap?>>()
            var products = toObjects(value, Product::class.java)
            var productsTaskCounter = 0

            products = products.filter { product -> product.reporters.size < REPORT_THRESHOLD }

            if (products.isEmpty()) {
                productsContainer.value = ProductsContainer(exception, list)
                return@addSnapshotListener
            }

            products.forEach { product ->
                val path = product.imagesPaths.firstOrNull()

                if (path == null) {
                    list.add(product to null)
                    productsTaskCounter++

                    if (productsTaskCounter == products.size)
                        productsContainer.value = ProductsContainer(exception, list)

                    return@forEach
                }

                repository.getImage(path).addOnCompleteListener { task ->
                    val bytes = task.result
                    var bitmap: Bitmap? = null

                    if (!task.isSuccessful || bytes == null) {
                        if (exception == -1)
                            exception = R.string.ImageNotFoundException
                    } else
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    list.add(product to bitmap)

                    productsTaskCounter++

                    if (productsTaskCounter == products.size)
                        productsContainer.value = ProductsContainer(exception, list)
                }
            }

            error?.message?.let { Log.i(VTAG, it) }
        }

        return productsContainer
    }

    fun signIn(email: String, password: String): LiveData<Int> {
        return request(repository.signIn(email, password))
    }

    private fun getUser(email: String): LiveData<Pair<Int, User>> {
        repository.getUser(email).addSnapshotListener { value, error ->
            var exception = if (error == null) -1 else R.string.NetworkException
            var user = User()

            if (value != null && !value.isEmpty) {
                val document: QueryDocumentSnapshot = value.first()
                user = toObject(document, User::class.java)

                if (user.location.zipCode == null)
                    locations.value?.second?.let { locations ->
                        val location =
                            locations.firstOrNull { loc -> loc.city == user.location.city }

                        location?.zipCode?.let { zipCode ->
                            user.location.zipCode = zipCode
                            repository.updateUserLocation(document.id, zipCode)
                        }
                    }
            } else if (exception == -1)
                exception = R.string.NotFoundException

            this.user.value = exception to user
        }

        return user
    }

    fun getUserById(id: String): LiveData<Pair<Int, User>> {
        val userLiveData = MutableLiveData<Pair<Int, User>>()

        repository.getUserById(id).addOnCompleteListener { task ->
            val value = task.result
            var exception = if (task.isSuccessful) -1 else R.string.NetworkException
            var user = User()

            if (value != null) {
                user = toObject(value, User::class.java)
            } else if (exception == -1)
                exception = R.string.NotFoundException

            userLiveData.value = exception to user
        }

        return userLiveData
    }

    fun reportProduct(id: String, userId: String): LiveData<Int> {
        return request(repository.reportProduct(id, userId))
    }

    fun registerUser(email: String, password: String): LiveData<Int> {
        return request(repository.registerUser(email, password))
    }

    fun addUser(user: User): LiveData<Int> {
        return request(repository.addUser(user))
    }

    fun getCategories(): LiveData<Pair<Int, List<Category>>> {
        return requestList(repository.getCategories(), categories)
    }

    fun getLocations(): LiveData<Pair<Int, List<Location>>> {
        return requestList(repository.getLocations(), locations)
    }

    fun getLocation(city: String): LiveData<Pair<Int, Location>> {
        return Transformations.map(locations) { (exception, locations) ->
            if (exception == -1) {
                val location = locations.firstOrNull { loc -> loc.city == city }
                if (location != null)
                    exception to location
                else
                    R.string.NotFoundException to Location()
            } else
                exception to Location()
        }
    }

    fun getProduct(): LiveData<ProductContainer> {
        val imagesPaths = product.imagesPaths
        val productContainer = MutableLiveData<ProductContainer>()
        val images = arrayListOf<Bitmap>()
        var exception = -1
        var imagesTaskCounter = 0

        if (imagesPaths.isEmpty()) {
            productContainer.value = ProductContainer(exception, product, images)
            return productContainer
        }

        imagesPaths.forEach { path ->
            repository.getImage(path).addOnCompleteListener { task ->
                val bytes = task.result

                if (!task.isSuccessful || bytes == null)
                    exception = R.string.ImageNotFoundException
                else {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    images.add(bitmap)
                }

                imagesTaskCounter++

                if (imagesTaskCounter == imagesPaths.size)
                    productContainer.value = ProductContainer(exception, product, images)
            }
        }

        return productContainer
    }

    fun updateProduct(product: Product): LiveData<Int> {
        return request(repository.updateProduct(product))
    }

    fun updateLocation(locationId: String, zipCode: Long): LiveData<Int> {
        return request(repository.updateLocation(locationId, zipCode))
    }

    fun addConsultation(productId: String, consultation: Consultation): LiveData<Int> {
        return request(repository.addConsultation(productId, consultation))
    }

    fun addToFavorites(userId: String, favoriteId: String): LiveData<Int> {
        return request(repository.addToFavorites(userId, favoriteId))
    }

    fun removeFromFavorites(userId: String, favoriteId: String): LiveData<Int> {
        return request(repository.removeFromFavorites(userId, favoriteId))
    }

    fun addLocation(location: Location): LiveData<Int> {
        return request(repository.addLocation(location))
    }

    private fun addProductImages(
        product: Product,
        images: ArrayList<InputStream>
    ): LiveData<Pair<Int, Product>> {
        val productLiveData = MutableLiveData<Pair<Int, Product>>()
        val folder = UUID.randomUUID().toString()
        var imagesTaskCounter = 0
        var exception = -1

        if (images.isEmpty()) {
            productLiveData.value = exception to product
            return productLiveData
        }

        images.forEach { image ->
            val path = "${folder}/${UUID.randomUUID()}"

            repository.addImage(path, image).addOnCompleteListener { task ->
                if (task.isSuccessful)
                    product.imagesPaths.add(path)
                else
                    exception = R.string.ImageNotAddedException

                imagesTaskCounter++

                if (imagesTaskCounter == images.size)
                    productLiveData.value = exception to product
            }
        }

        return productLiveData
    }

    fun addProduct(product: Product, images: ArrayList<InputStream>): LiveData<Pair<Int, Product>> {
        val productImages = addProductImages(product, images)

        return Transformations.switchMap(productImages) { (exception, product) ->
            val productAdding = productAdding(product, exception)

            Transformations.map(productAdding) { (exception2, product2) ->
                exception2 to product2
            }
        }
    }

    private fun productAdding(product: Product, exception: Int): LiveData<Pair<Int, Product>> {
        val productLiveData = MutableLiveData<Pair<Int, Product>>()

        repository.addProduct(product).addOnCompleteListener { task ->
            val productData = task.result
            var exception2 =
                if (task.isSuccessful && productData != null) -1 else R.string.NetworkException

            if (productData != null)
                product.id = productData.id

            if (exception2 == -1 && exception != -1)
                exception2 = exception

            productLiveData.value = exception2 to product
        }

        return productLiveData
    }

    fun deleteProduct(id: String, imagesPaths: ArrayList<String>): LiveData<Int> {
        deleteImages(imagesPaths)

        return request(repository.deleteProduct(id))
    }

    private fun deleteProducts(ownerId: String?): LiveData<Int> {
        val exceptionLiveData = MutableLiveData<Int>()

        if (ownerId == null) {
            exceptionLiveData.value = R.string.NotFoundException
            return exceptionLiveData
        }

        repository.getProducts(SearchQuery(ownerId = ownerId)).get().addOnCompleteListener { task ->
            var exception = if (task.isSuccessful) -1 else R.string.ProductNotDeletedException
            val products = task.result

            if (products == null || products.isEmpty)
                exceptionLiveData.value = exception
            else
                products.let { docs ->
                    val ids = docs.map { doc -> doc.id }.toTypedArray()

                    docs.forEach { document ->
                        val product = toObject(document, Product::class.java)

                        deleteImages(product.imagesPaths)
                    }

                    repository.deleteProducts(*ids).addOnCompleteListener { task2 ->
                        val exception2 =
                            if (task2.isSuccessful) -1 else R.string.ProductNotDeletedException

                        if (exception == -1 && exception2 != -1)
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

    fun deleteUser(user: User): LiveData<Int> {
        val deleteProducts = deleteProducts(user.id)

        return Transformations.switchMap(deleteProducts) { exception ->
            if (exception == -1) {
                val deleteFirebaseUser = request(repository.deleteUser())

                Transformations.map(deleteFirebaseUser) { exception2 ->
                    exception2
                }
            } else
                MutableLiveData(exception)
        }
    }

    fun signOut() {
        repository.signOut()
    }

    fun getDiscussions(userId: String): LiveData<Pair<Int, List<Discussion>>> {
        return requestList(repository.getDiscussions(userId), discussions)
    }

    fun updateDiscussion(id: String, message: Message): LiveData<Int> {
        return request(repository.updateDiscussion(id, message))
    }

    fun addDiscussion(discussion: Discussion): MutableLiveData<Pair<Int, Discussion>> {
        val discussionLiveData = MutableLiveData<Pair<Int, Discussion>>()

        repository.addDiscussion(discussion).addOnCompleteListener { task ->
            val discussionData = task.result
            val exception =
                if (task.isSuccessful && discussionData != null) -1 else R.string.NetworkException

            if (discussionData != null)
                discussion.id = discussionData.id

            discussionLiveData.value = exception to discussion
        }

        return discussionLiveData
    }

    fun getDiscussion(id: String): LiveData<Pair<Int, Discussion>> {
        return Transformations.map(discussions) { (exception, discussions) ->
            if (exception == -1) {
                val discussion = discussions.firstOrNull { conv -> conv.id == id }

                if (discussion != null)
                    exception to discussion
                else
                    R.string.NotFoundException to Discussion()
            } else
                exception to Discussion()
        }
    }

    fun getDiscussionId(discussion: Discussion, discussions: List<Discussion>): String? {
        return discussions.find { conv ->
            discussion.productId == conv.productId
                    && discussion.userId == conv.userId
                    && discussion.ownerId == conv.ownerId
        }?.id
    }

    fun resetPassword(email: String): LiveData<Int> {
        return request(repository.resetPassword(email))
    }

    fun updateUser(user: User): LiveData<Int> {
        return request(repository.updateUser(user))
    }

    private inline fun <reified T : Entity> requestList(
        query: Query,
        liveData: MutableLiveData<Pair<Int, List<T>>>
    ): LiveData<Pair<Int, List<T>>> {
        query.addSnapshotListener { documents, error ->
            val exception = if (error == null) -1 else R.string.NetworkException
            val values: ArrayList<T> = ArrayList()

            documents?.forEach { document ->
                val value = toObject(document, T::class.java)
                values.add(value)
            }

            liveData.value = exception to values
        }

        return liveData
    }

    private fun <T> request(request: Task<T>?): LiveData<Int> {
        val exceptionLiveData = MutableLiveData<Int>()

        if (request == null) {
            exceptionLiveData.value = R.string.NotFoundException
            return exceptionLiveData
        }

        request.addOnCompleteListener { task ->
            var exception = if (task.isSuccessful) -1 else R.string.NetworkException
            when(task.exception){
                is FirebaseAuthInvalidCredentialsException -> exception = R.string.password_email_exception
                is FirebaseAuthInvalidUserException -> exception = R.string.invalid_user_exception
                is FirebaseAuthUserCollisionException -> exception = R.string.conflict_user_exception
                is FirebaseAuthEmailException -> exception = R.string.auth_email_exception
                is FirebaseAuthActionCodeException -> exception = R.string.expired_code_exception
                is FirebaseAuthWeakPasswordException -> exception = R.string.weak_password_exception
                is FirebaseAuthWebException -> exception = R.string.incomplete_operation_exception
                is FirebaseAuthException -> exception = R.string.AuthentificationException
                is FirebaseNetworkException -> exception = R.string.NetworkException
                is FirebaseNoSignedInUserException -> exception = R.string.sign_in_user_exception
                is FirebaseFirestoreException -> {
                    val currentException = task.exception as FirebaseFirestoreException
                    when(currentException.code){
                        FirebaseFirestoreException.Code.ABORTED -> exception = R.string.aborted
                        FirebaseFirestoreException.Code.CANCELLED -> exception = R.string.canceled
                        FirebaseFirestoreException.Code.DATA_LOSS -> exception = R.string.data_loss_exception
                        FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception = R.string.already_exist_exception
                        FirebaseFirestoreException.Code.INTERNAL -> exception = R.string.internal_exception
                        FirebaseFirestoreException.Code.NOT_FOUND -> exception = R.string.NotFoundException
                        FirebaseFirestoreException.Code.UNKNOWN -> exception = R.string.unknown_exception
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception = R.string.permission_denied_exception
                        FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception = R.string.unauthentificated_exception
                        else->exception = R.string.firestore_exception
                    }
                }
            }
            exceptionLiveData.value = exception
        }

        return exceptionLiveData
    }

    private inline fun <reified T : Entity> toObject(
        document: DocumentSnapshot,
        @NonNull type: Class<T>
    ): T {
        val value: T
        val obj = document.toObject(type)

        if (obj == null)
            value = T::class.java.newInstance()
        else {
            obj.id = document.id
            value = obj
        }

        return value
    }

    private inline fun <reified T : Entity> toObjects(
        documents: QuerySnapshot?,
        @NonNull type: Class<T>
    ): List<T> {
        val value = ArrayList<T>()

        documents?.forEach { document ->
            value.add(toObject(document, type))
        }

        return value
    }
}

class ProductContainer(
    val exception: Int,
    val product: Product,
    val images: ArrayList<Bitmap>
) {
    operator fun component1(): Int {
        return exception
    }

    operator fun component2(): Product {
        return product
    }

    operator fun component3(): ArrayList<Bitmap> {
        return images
    }
}

class ProductsContainer(
    val exception: Int,
    val products: List<Pair<Product, Bitmap?>>
) {
    operator fun component1(): Int {
        return exception
    }

    operator fun component2(): List<Pair<Product, Bitmap?>> {
        return products
    }
}