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
    var discussionId: String = ""
    var product: Product = Product()
    var searchQuery: SearchQuery = SearchQuery()
    val user: MutableLiveData<Pair<Int, User>> = state.getLiveData("user")
    val categories: MutableLiveData<Pair<Int, List<Category>>> = state.getLiveData("categories")
    val locations: MutableLiveData<Pair<Int, List<Location>>> = state.getLiveData("locations")
    val discussions: MutableLiveData<Pair<Int, List<Discussion>>> = state.getLiveData("discussions")

    init {
        getCategories()
        getLocations()
    }

    fun getMenu(): MutableLiveData<Menu> {
        return MutableLiveData()
    }

    /**
     * Check if user is available
     *
     * @return if user is available
     */
    private fun isUserAvailable(): Boolean {
        user.value?.let { (exception, user) ->
            if (exception == -1 && user.emailAddress.isNotEmpty())
                return true
        }

        return false
    }

    /**
     * Set User State
     *
     * @param isUserSignedIn
     * @param email
     */
    fun setUserState(isUserSignedIn: Boolean, email: String? = null) {
        this.isUserSignedIn = isUserSignedIn

        when (isUserSignedIn) {
            false -> user.value = R.string.SignedOutException to User()
            true -> if (!isUserAvailable()) email?.let { mail -> getUser(mail) }
        }
    }

    /**
     * Get product from Firebase
     *
     * @param search
     * @return productsContainer
     */
    fun getProducts(search: SearchQuery): MutableLiveData<ProductsContainer> {
        val productsContainer = MutableLiveData<ProductsContainer>()
        val query = repository.getProducts(search)

        query.addSnapshotListener { value, error ->
            var exception = if (error == null) -1 else R.string.FirestoreException
            when(error?.code){
                FirebaseFirestoreException.Code.ABORTED -> exception = R.string.CancelledException
                FirebaseFirestoreException.Code.CANCELLED -> exception = R.string.CancelledException
                FirebaseFirestoreException.Code.DATA_LOSS -> exception = R.string.DataLossException
                FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception = R.string.AlreadyExistsException
                FirebaseFirestoreException.Code.INTERNAL -> exception = R.string.InternalException
                FirebaseFirestoreException.Code.NOT_FOUND -> exception = R.string.NotFoundException
                FirebaseFirestoreException.Code.UNKNOWN -> exception = R.string.UnknownException
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception = R.string.PermissionDeniedException
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception = R.string.UnauthentificatedException
            }
            val list = arrayListOf<Pair<Product, Bitmap?>>()
            var products = toObjects(value, Product::class.java)
            if(search.title!=null)products = products.filter { product -> product.title.contains(search.title)  }
            if(search.priceMin!=null)products = products.filter { product -> product.price >= search.priceMin }
            if(search.priceMax!=null)products = products.filter { product -> product.price <= search.priceMax }
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

    /**
     * Authentificate User
     *
     * @param email
     * @param password
     * @return errorCode
     */
    fun signIn(email: String, password: String): LiveData<Int> {
        return request(repository.signIn(email, password))
    }

    /**
     * Get User from Firebase using email
     *
     * @param email
     * @return LiveData<Pair<errorCode,User>>
     */
    private fun getUser(email: String): LiveData<Pair<Int, User>> {
        repository.getUser(email).addSnapshotListener { value, error ->
            var exception = if (error == null) -1 else R.string.FirestoreException
            when(error?.code){
                FirebaseFirestoreException.Code.ABORTED -> exception = R.string.CancelledException
                FirebaseFirestoreException.Code.CANCELLED -> exception = R.string.CancelledException
                FirebaseFirestoreException.Code.DATA_LOSS -> exception = R.string.DataLossException
                FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception = R.string.AlreadyExistsException
                FirebaseFirestoreException.Code.INTERNAL -> exception = R.string.InternalException
                FirebaseFirestoreException.Code.NOT_FOUND -> exception = R.string.NotFoundException
                FirebaseFirestoreException.Code.UNKNOWN -> exception = R.string.UnknownException
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception = R.string.PermissionDeniedException
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception = R.string.UnauthentificatedException
            }
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

    /**
     * Get User from Firebase using id
     *
     * @param id
     * @return LiveData<Pair<errorCode,User>>
     */
    fun getUserById(id: String): LiveData<Pair<Int, User>> {
        val userLiveData = MutableLiveData<Pair<Int, User>>()

        repository.getUserById(id).addOnCompleteListener { task ->
            val value = task.result
            var exception = if (task.isSuccessful) -1 else R.string.NetworkException
            when(task.exception){
                is FirebaseAuthInvalidCredentialsException -> exception = R.string.AuthCredentialsException
                is FirebaseAuthInvalidUserException -> exception = R.string.InvalidUserException
                is FirebaseAuthUserCollisionException -> exception = R.string.ConflictUserException
                is FirebaseAuthEmailException -> exception = R.string.AuthEmailException
                is FirebaseAuthActionCodeException -> exception = R.string.ExpirationException
                is FirebaseAuthWeakPasswordException -> exception = R.string.WeakPasswordException
                is FirebaseAuthWebException -> exception = R.string.IncompleteOperationException
                is FirebaseAuthException -> exception = R.string.AuthentificationException
                is FirebaseNetworkException -> exception = R.string.NetworkException
                is FirebaseNoSignedInUserException -> exception = R.string.SignedOutException
                is FirebaseFirestoreException -> {
                    val currentException = task.exception as FirebaseFirestoreException
                    when(currentException.code){
                        FirebaseFirestoreException.Code.ABORTED -> exception = R.string.CancelledException
                        FirebaseFirestoreException.Code.CANCELLED -> exception = R.string.CancelledException
                        FirebaseFirestoreException.Code.DATA_LOSS -> exception = R.string.DataLossException
                        FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception = R.string.AlreadyExistsException
                        FirebaseFirestoreException.Code.INTERNAL -> exception = R.string.InternalException
                        FirebaseFirestoreException.Code.NOT_FOUND -> exception = R.string.NotFoundException
                        FirebaseFirestoreException.Code.UNKNOWN -> exception = R.string.UnknownException
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception = R.string.PermissionDeniedException
                        FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception = R.string.UnauthentificatedException
                        else->exception = R.string.FirestoreException
                    }
                }
            }
            var user = User()

            if (value != null) {
                user = toObject(value, User::class.java)
            } else if (exception == -1)
                exception = R.string.NotFoundException

            userLiveData.value = exception to user
        }

        return userLiveData
    }

    /**
     * Report product by adding productId and userId in database
     *
     * @param id
     * @param userId
     * @return errorCode
     */
    fun reportProduct(id: String, userId: String): LiveData<Int> {
        return request(repository.reportProduct(id, userId))
    }

    /**
     * Register user in Firebase with email and password
     *
     * @param email
     * @param password
     * @return errorCode
     */
    fun registerUser(email: String, password: String): LiveData<Int> {
        return request(repository.registerUser(email, password))
    }

    /**
     * Add User's information in Firebase
     *
     * @param user
     * @return errorCode
     */
    fun addUser(user: User): LiveData<Int> {
        return request(repository.addUser(user))
    }

    private fun getCategories(): LiveData<Pair<Int, List<Category>>> {
        return requestList(repository.getCategories(), categories)
    }

    /**
     * Get locations from Firebase
     *
     * @return LiveData<Pair<errorCode, LocationsList>>
     */
    private fun getLocations(): LiveData<Pair<Int, List<Location>>> {
        return requestList(repository.getLocations(), locations)
    }

    /**
     * Get location from Firebase using city name
     *
     * @param city
     * @return LiveData<Pair<errorCode, location>>
     */
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

    /**
     * Get product from Firebase using product attribute
     *
     * @return LiveData<productContainer>
     */
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

    /**
     * Update a product
     *
     * @param product
     * @return errorCode
     */
    fun updateProduct(product: Product): LiveData<Int> {
        return request(repository.updateProduct(product))
    }

    /**
     * Update location's zipcode
     *
     * @param locationId
     * @param zipCode
     * @return errorCode
     */
    fun updateLocation(locationId: String, zipCode: Long): LiveData<Int> {
        return request(repository.updateLocation(locationId, zipCode))
    }

    /**
     * Add to Firebase a consultation
     *
     * @param productId
     * @param consultation
     * @return errorCode
     */
    fun addConsultation(productId: String, consultation: Consultation): LiveData<Int> {
        return request(repository.addConsultation(productId, consultation))
    }

    /**
     * Add a product to the current user's favorites in Firebase
     *
     * @param userId
     * @param favoriteId
     * @return errorCode
     */
    fun addToFavorites(userId: String, favoriteId: String): LiveData<Int> {
        return request(repository.addToFavorites(userId, favoriteId))
    }

    /**
     * Remove a product from current user's favorites in Firebase
     *
     * @param userId
     * @param favoriteId
     * @return errorCode
     */
    fun removeFromFavorites(userId: String, favoriteId: String): LiveData<Int> {
        return request(repository.removeFromFavorites(userId, favoriteId))
    }

    /**
     * add location to Firebase
     *
     * @param location
     * @return errorCode
     */
    fun addLocation(location: Location): LiveData<Int> {
        return request(repository.addLocation(location))
    }

    /**
     * Add product's images to Firebase
     *
     * @param product
     * @param images
     * @return LiveData<Pair<errorCode,product>>
     */
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

    /**
     * Add product to Firebase
     *
     * @param product
     * @param images
     * @return LiveData<Pair<errorCode,product>>
     */
    fun addProduct(product: Product, images: ArrayList<InputStream>): LiveData<Pair<Int, Product>> {
        val productImages = addProductImages(product, images)

        return Transformations.switchMap(productImages) { (exception, product) ->
            val productAdding = productAdding(product, exception)

            Transformations.map(productAdding) { (exception2, product2) ->
                exception2 to product2
            }
        }
    }

    /**
     * Add product to Firebase
     *
     * @param product
     * @param exception
     * @return LiveData<Pair<Int,Product>>
     */
    private fun productAdding(product: Product, exception: Int): LiveData<Pair<Int, Product>> {
        val productLiveData = MutableLiveData<Pair<Int, Product>>()

        repository.addProduct(product).addOnCompleteListener { task ->
            val productData = task.result
            var exception2 =
                if (task.isSuccessful && productData != null) -1 else R.string.NetworkException
            when(task.exception){
                is FirebaseAuthInvalidCredentialsException -> exception2 = R.string.AuthCredentialsException
                is FirebaseAuthInvalidUserException -> exception2 = R.string.InvalidUserException
                is FirebaseAuthUserCollisionException -> exception2 = R.string.ConflictUserException
                is FirebaseAuthEmailException -> exception2 = R.string.AuthEmailException
                is FirebaseAuthActionCodeException -> exception2 = R.string.ExpirationException
                is FirebaseAuthWeakPasswordException -> exception2 = R.string.WeakPasswordException
                is FirebaseAuthWebException -> exception2 = R.string.IncompleteOperationException
                is FirebaseAuthException -> exception2 = R.string.AuthentificationException
                is FirebaseNetworkException -> exception2 = R.string.NetworkException
                is FirebaseNoSignedInUserException -> exception2 = R.string.SignedOutException
                is FirebaseFirestoreException -> {
                    val currentException = task.exception as FirebaseFirestoreException
                    when(currentException.code){
                        FirebaseFirestoreException.Code.ABORTED -> exception2 = R.string.CancelledException
                        FirebaseFirestoreException.Code.CANCELLED -> exception2 = R.string.CancelledException
                        FirebaseFirestoreException.Code.DATA_LOSS -> exception2 = R.string.DataLossException
                        FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception2 = R.string.AlreadyExistsException
                        FirebaseFirestoreException.Code.INTERNAL -> exception2 = R.string.InternalException
                        FirebaseFirestoreException.Code.NOT_FOUND -> exception2 = R.string.NotFoundException
                        FirebaseFirestoreException.Code.UNKNOWN -> exception2 = R.string.UnknownException
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception2 = R.string.PermissionDeniedException
                        FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception2 = R.string.UnauthentificatedException
                        else->exception2 = R.string.FirestoreException
                    }
                }
            }
            if (productData != null)
                product.id = productData.id

            if (exception2 == -1 && exception != -1)
                exception2 = exception

            productLiveData.value = exception2 to product
        }

        return productLiveData
    }

    /**
     * Delete product and images from Firebase
     *
     * @param id
     * @param imagesPaths
     * @return errorCode
     */
    fun deleteProduct(id: String, imagesPaths: ArrayList<String>): LiveData<Int> {
        deleteImages(imagesPaths)

        return request(repository.deleteProduct(id))
    }

    /**
     * Delete products and images using ownerId from Firebase
     *
     * @param ownerId
     * @return errorCode
     */
    private fun deleteProducts(ownerId: String?): LiveData<Int> {
        val exceptionLiveData = MutableLiveData<Int>()

        if (ownerId == null) {
            exceptionLiveData.value = R.string.NotFoundException
            return exceptionLiveData
        }

        repository.getProducts(SearchQuery(ownerId = ownerId)).get().addOnCompleteListener { task ->
            var exception = if (task.isSuccessful) -1 else R.string.ProductNotDeletedException
            when(task.exception){
                is FirebaseAuthInvalidCredentialsException -> exception = R.string.AuthCredentialsException
                is FirebaseAuthInvalidUserException -> exception = R.string.InvalidUserException
                is FirebaseAuthUserCollisionException -> exception = R.string.ConflictUserException
                is FirebaseAuthEmailException -> exception = R.string.AuthEmailException
                is FirebaseAuthActionCodeException -> exception = R.string.ExpirationException
                is FirebaseAuthWeakPasswordException -> exception = R.string.WeakPasswordException
                is FirebaseAuthWebException -> exception = R.string.IncompleteOperationException
                is FirebaseAuthException -> exception = R.string.AuthentificationException
                is FirebaseNetworkException -> exception = R.string.NetworkException
                is FirebaseNoSignedInUserException -> exception = R.string.SignedOutException
                is FirebaseFirestoreException -> {
                    val currentException = task.exception as FirebaseFirestoreException
                    when(currentException.code){
                        FirebaseFirestoreException.Code.ABORTED -> exception = R.string.CancelledException
                        FirebaseFirestoreException.Code.CANCELLED -> exception = R.string.CancelledException
                        FirebaseFirestoreException.Code.DATA_LOSS -> exception = R.string.DataLossException
                        FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception = R.string.AlreadyExistsException
                        FirebaseFirestoreException.Code.INTERNAL -> exception = R.string.InternalException
                        FirebaseFirestoreException.Code.NOT_FOUND -> exception = R.string.NotFoundException
                        FirebaseFirestoreException.Code.UNKNOWN -> exception = R.string.UnknownException
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception = R.string.PermissionDeniedException
                        FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception = R.string.UnauthentificatedException
                        else->exception = R.string.FirestoreException
                    }
                }
            }
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
                        var exception2 =
                            if (task2.isSuccessful) -1 else R.string.ProductNotDeletedException
                        when(task2.exception){
                            is FirebaseAuthInvalidCredentialsException -> exception2 = R.string.AuthCredentialsException
                            is FirebaseAuthInvalidUserException -> exception2 = R.string.InvalidUserException
                            is FirebaseAuthUserCollisionException -> exception2 = R.string.ConflictUserException
                            is FirebaseAuthEmailException -> exception2 = R.string.AuthEmailException
                            is FirebaseAuthActionCodeException -> exception2 = R.string.ExpirationException
                            is FirebaseAuthWeakPasswordException -> exception2 = R.string.WeakPasswordException
                            is FirebaseAuthWebException -> exception2 = R.string.IncompleteOperationException
                            is FirebaseAuthException -> exception2 = R.string.AuthentificationException
                            is FirebaseNetworkException -> exception2 = R.string.NetworkException
                            is FirebaseNoSignedInUserException -> exception2 = R.string.SignedOutException
                            is FirebaseFirestoreException -> {
                                val currentException = task2.exception as FirebaseFirestoreException
                                when(currentException.code){
                                    FirebaseFirestoreException.Code.ABORTED -> exception2 = R.string.CancelledException
                                    FirebaseFirestoreException.Code.CANCELLED -> exception2 = R.string.CancelledException
                                    FirebaseFirestoreException.Code.DATA_LOSS -> exception2 = R.string.DataLossException
                                    FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception2 = R.string.AlreadyExistsException
                                    FirebaseFirestoreException.Code.INTERNAL -> exception2 = R.string.InternalException
                                    FirebaseFirestoreException.Code.NOT_FOUND -> exception2 = R.string.NotFoundException
                                    FirebaseFirestoreException.Code.UNKNOWN -> exception2 = R.string.UnknownException
                                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception2 = R.string.PermissionDeniedException
                                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception2 = R.string.UnauthentificatedException
                                    else->exception2 = R.string.FirestoreException
                                }
                            }
                        }

                        if (exception == -1 && exception2 != -1)
                            exception = exception2

                        exceptionLiveData.value = exception
                    }
                }
        }

        return exceptionLiveData
    }

    /**
     * Delete images using path from Firebase
     *
     * @param paths
     */
    private fun deleteImages(paths: ArrayList<String>) {
        paths.forEach { path ->
            repository.deleteImage(path)
        }
    }

    /**
     * Delete User from firebase
     *
     * @param user
     * @return errorCode
     */
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

    /**
     * Unauthenficate current user
     *
     */
    fun signOut() {
        repository.signOut()
    }

    /**
     * Get discussions from Firebase using current user's Id
     *
     * @param userId
     * @return Livedata<Pair<errorCode,discussions>>
     */
    fun getDiscussions(userId: String): LiveData<Pair<Int, List<Discussion>>> {
        return requestList(repository.getDiscussions(userId), discussions)
    }

    /**
     * Add a new Message to a discussion then update the discussion in Firebase
     *
     * @param id
     * @param message
     * @return errorCode
     */
    fun updateDiscussion(id: String, message: Message): LiveData<Int> {
        return request(repository.updateDiscussion(id, message))
    }

    /**
     * Add a new discussion to Firebase
     *
     * @param discussion
     * @return LiveData<Pair<errorCode,discussion>>
     */
    fun addDiscussion(discussion: Discussion): MutableLiveData<Pair<Int, Discussion>> {
        val discussionLiveData = MutableLiveData<Pair<Int, Discussion>>()

        repository.addDiscussion(discussion).addOnCompleteListener { task ->
            val discussionData = task.result
            var exception =
                if (task.isSuccessful && discussionData != null) -1 else R.string.NetworkException
            when(task.exception){
                is FirebaseAuthInvalidCredentialsException -> exception = R.string.AuthCredentialsException
                is FirebaseAuthInvalidUserException -> exception = R.string.InvalidUserException
                is FirebaseAuthUserCollisionException -> exception = R.string.ConflictUserException
                is FirebaseAuthEmailException -> exception = R.string.AuthEmailException
                is FirebaseAuthActionCodeException -> exception = R.string.ExpirationException
                is FirebaseAuthWeakPasswordException -> exception = R.string.WeakPasswordException
                is FirebaseAuthWebException -> exception = R.string.IncompleteOperationException
                is FirebaseAuthException -> exception = R.string.AuthentificationException
                is FirebaseNetworkException -> exception = R.string.NetworkException
                is FirebaseNoSignedInUserException -> exception = R.string.SignedOutException
                is FirebaseFirestoreException -> {
                    val currentException = task.exception as FirebaseFirestoreException
                    when(currentException.code){
                        FirebaseFirestoreException.Code.ABORTED -> exception = R.string.CancelledException
                        FirebaseFirestoreException.Code.CANCELLED -> exception = R.string.CancelledException
                        FirebaseFirestoreException.Code.DATA_LOSS -> exception = R.string.DataLossException
                        FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception = R.string.AlreadyExistsException
                        FirebaseFirestoreException.Code.INTERNAL -> exception = R.string.InternalException
                        FirebaseFirestoreException.Code.NOT_FOUND -> exception = R.string.NotFoundException
                        FirebaseFirestoreException.Code.UNKNOWN -> exception = R.string.UnknownException
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception = R.string.PermissionDeniedException
                        FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception = R.string.UnauthentificatedException
                        else->exception = R.string.FirestoreException
                    }
                }
            }

            if (discussionData != null)
                discussion.id = discussionData.id

            discussionLiveData.value = exception to discussion
        }

        return discussionLiveData
    }

    /**
     * Get discussion from Firebase using id
     *
     * @param id
     * @return Livedata<Pair<errorCode,discussion>>
     */
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

    /**
     * Get a discussion's id from Firebase
     *
     * @param discussion
     * @param discussions
     * @return Id
     */
    fun getDiscussionId(discussion: Discussion, discussions: List<Discussion>): String? {
        return discussions.find { conv ->
            discussion.productId == conv.productId
                    && discussion.userId == conv.userId
                    && discussion.ownerId == conv.ownerId
        }?.id
    }

    /**
     * reset current user's password
     *
     * @param email
     * @return errorCode
     */
    fun resetPassword(email: String): LiveData<Int> {
        return request(repository.resetPassword(email))
    }

    /**
     * update current user's information
     *
     * @param user
     * @return errorCode
     */
    fun updateUser(user: User): LiveData<Int> {
        return request(repository.updateUser(user))
    }

    /**
     * Execute the query
     *
     * @param T
     * @param query
     * @param liveData
     * @return Objects list or EmptyList + errorCode
     */
    private inline fun <reified T : Entity> requestList(
        query: Query,
        liveData: MutableLiveData<Pair<Int, List<T>>>
    ): LiveData<Pair<Int, List<T>>> {
        query.addSnapshotListener { documents, error ->
            var exception = if (error == null) -1 else R.string.FirestoreException
            when(error?.code){
                FirebaseFirestoreException.Code.ABORTED -> exception = R.string.CancelledException
                FirebaseFirestoreException.Code.CANCELLED -> exception = R.string.CancelledException
                FirebaseFirestoreException.Code.DATA_LOSS -> exception = R.string.DataLossException
                FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception = R.string.AlreadyExistsException
                FirebaseFirestoreException.Code.INTERNAL -> exception = R.string.InternalException
                FirebaseFirestoreException.Code.NOT_FOUND -> exception = R.string.NotFoundException
                FirebaseFirestoreException.Code.UNKNOWN -> exception = R.string.UnknownException
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception = R.string.PermissionDeniedException
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception = R.string.UnauthentificatedException
            }

            val values: ArrayList<T> = ArrayList()

            documents?.forEach { document ->
                val value = toObject(document, T::class.java)
                values.add(value)
            }

            liveData.value = exception to values
        }

        return liveData
    }

    /**
     * Execute the query
     *
     * @param T
     * @param request
     * @return errorCode
     */
    private fun <T> request(request: Task<T>?): LiveData<Int> {
        val exceptionLiveData = MutableLiveData<Int>()

        if (request == null) {
            exceptionLiveData.value = R.string.NotFoundException
            return exceptionLiveData
        }

        request.addOnCompleteListener { task ->
            var exception = if (task.isSuccessful) -1 else R.string.NetworkException
            when(task.exception){
                is FirebaseAuthInvalidCredentialsException -> exception = R.string.AuthCredentialsException
                is FirebaseAuthInvalidUserException -> exception = R.string.InvalidUserException
                is FirebaseAuthUserCollisionException -> exception = R.string.ConflictUserException
                is FirebaseAuthEmailException -> exception = R.string.AuthEmailException
                is FirebaseAuthActionCodeException -> exception = R.string.ExpirationException
                is FirebaseAuthWeakPasswordException -> exception = R.string.WeakPasswordException
                is FirebaseAuthWebException -> exception = R.string.IncompleteOperationException
                is FirebaseAuthException -> exception = R.string.AuthentificationException
                is FirebaseNetworkException -> exception = R.string.NetworkException
                is FirebaseNoSignedInUserException -> exception = R.string.SignedOutException
                is FirebaseFirestoreException -> {
                    val currentException = task.exception as FirebaseFirestoreException
                    when(currentException.code){
                        FirebaseFirestoreException.Code.ABORTED -> exception = R.string.CancelledException
                        FirebaseFirestoreException.Code.CANCELLED -> exception = R.string.CancelledException
                        FirebaseFirestoreException.Code.DATA_LOSS -> exception = R.string.DataLossException
                        FirebaseFirestoreException.Code.ALREADY_EXISTS -> exception = R.string.AlreadyExistsException
                        FirebaseFirestoreException.Code.INTERNAL -> exception = R.string.InternalException
                        FirebaseFirestoreException.Code.NOT_FOUND -> exception = R.string.NotFoundException
                        FirebaseFirestoreException.Code.UNKNOWN -> exception = R.string.UnknownException
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> exception = R.string.PermissionDeniedException
                        FirebaseFirestoreException.Code.UNAUTHENTICATED -> exception = R.string.UnauthentificatedException
                        else->exception = R.string.FirestoreException
                    }
                }
            }
            exceptionLiveData.value = exception
        }

        return exceptionLiveData
    }

    /**
     * Convert Firebase's document to Object
     *
     * @param T
     * @param document
     * @param type
     * @return Model
     */
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

    /**
     * Convert Firebase's documents to List
     *
     * @param T
     * @param documents
     * @param type
     * @return List of Model
     */
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

/**
 * Product model + images
 *
 * @property exception
 * @property product
 * @property images
 */
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

/**
 * List of Products + image
 *
 * @property exception
 * @property products
 */
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