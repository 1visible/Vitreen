package c0d3.vitreen.app.utils

import c0d3.vitreen.app.models.*
import c0d3.vitreen.app.utils.Constants.Companion.CATEGORIES_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.DISCUSSIONS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.DOCUMENTS_LIMIT
import c0d3.vitreen.app.utils.Constants.Companion.IMAGE_SIZE
import c0d3.vitreen.app.utils.Constants.Companion.LOCATIONS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.PRODUCTS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.USERS_COLLECTION
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.InputStream
import java.util.*

class FirestoreRepository {
    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth
    private val storage = Firebase.storage

    /**
     * Create a query to get products from Firebase
     *
     * @param search
     * @return Query
     */
    fun getProducts(search: SearchQuery): Query {
        val (_, _, _, _, location, category, ownerId, ids) = search
        var query: Query = db.collection(PRODUCTS_COLLECTION)
        var limit = true

        if (location != null) {
            query = query.whereEqualTo("location", location)
            limit = false
        }

        if (category != null) {
            query = query.whereEqualTo("category", category)
            limit = false
        }

        if (ownerId != null) {
            query = query.whereEqualTo("ownerId", ownerId)
            limit = false
        }

        if (!ids.isNullOrEmpty()) {
            query = query.whereIn(FieldPath.documentId(), ids)
            limit = false
        }

        if (limit)
            query = query.limit(DOCUMENTS_LIMIT)

        return query
    }

    /**
     * Delete a product from Firebase
     *
     * @param id
     * @return Task
     */
    fun deleteProduct(id: String): Task<Void> {
        return db.collection(PRODUCTS_COLLECTION).document(id).delete()
    }

    /**
     * Sign in user with email and password
     *
     * @param email
     * @param password
     * @return Task
     */
    fun signIn(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    /**
     * Sign out the current user
     *
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Create a query to get current user's informations using his/her email address
     *
     * @param email
     * @return Query
     */
    fun getUser(email: String): Query {
        return db.collection(USERS_COLLECTION).whereEqualTo("emailAddress", email).limit(1)
    }

    /**
     * Get current user's informations using his/her id
     *
     * @param id
     * @return Task
     */
    fun getUserById(id: String): Task<DocumentSnapshot> {
        return db.collection(USERS_COLLECTION).document(id).get()
    }

    /**
     * Report product by adding productId and userId in database
     *
     * @param id
     * @param userId
     * @return Task
     */
    fun reportProduct(id: String, userId: String): Task<Void> {
        return db.collection(PRODUCTS_COLLECTION).document(id)
            .update("reporters", FieldValue.arrayUnion(userId))
    }

    /**
     * Create a user account with email and password
     *
     * @param email
     * @param password
     * @return Task
     */
    fun registerUser(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    /**
     * Add user's informations to Firebase
     *
     * @param user
     * @return Task
     */
    fun addUser(user: User): Task<DocumentReference> {
        return db.collection(USERS_COLLECTION).add(user)
    }

    /**
     * Create a query to get categories from Firebase
     *
     * @return Query
     */
    fun getCategories(): Query {
        return db.collection(CATEGORIES_COLLECTION)
    }

    /**
     * Create a query to get locations from Firebase
     *
     * @return Query
     */
    fun getLocations(): Query {
        return db.collection(LOCATIONS_COLLECTION)
    }

    /**
     * Get images from Firebase Storage using image path
     *
     * @param path
     * @return Task
     */
    fun getImage(path: String): Task<ByteArray> {
        return storage.reference.child("images/${path}").getBytes(IMAGE_SIZE)
    }

    /**
     * Update location's zipcode
     *
     * @param id
     * @param zipCode
     * @return Task
     */
    fun updateLocation(id: String, zipCode: Long): Task<Void> {
        return db.collection(LOCATIONS_COLLECTION).document(id).update("zipCode", zipCode)
    }

    /**
     * Update user's location zipcode
     *
     * @param id
     * @param zipCode
     * @return Task
     */
    fun updateUserLocation(id: String, zipCode: Long): Task<Void> {
        return db.collection(USERS_COLLECTION).document(id).update("location.zipCode", zipCode)
    }

    /**
     * Update product's informations
     *
     * @param product
     * @return Task
     */
    fun updateProduct(product: Product): Task<Void> {
        return db.collection(PRODUCTS_COLLECTION).document(product.id!!).update(
            "title",
            product.title,
            "description",
            product.description,
            "price",
            product.price,
            "brand",
            product.brand,
            "size",
            product.size,
            "location",
            product.location,
            "category",
            product.category,
            "modifiedAt",
            Calendar.getInstance().time
        )
    }

    /**
     * Add consultation to a product in Firebase
     *
     * @param productId
     * @param consultation
     * @return Task
     */
    fun addConsultation(productId: String, consultation: Consultation): Task<Void> {
        return db.collection(PRODUCTS_COLLECTION).document(productId)
            .update("consultations", FieldValue.arrayUnion(consultation))
    }

    /**
     * Add a product to user's favorites in Firebase
     *
     * @param userId
     * @param favoriteId
     * @return Task
     */
    fun addToFavorites(userId: String, favoriteId: String): Task<Void> {
        return db.collection(USERS_COLLECTION).document(userId)
            .update("favoritesIds", FieldValue.arrayUnion(favoriteId))
    }

    /**
     * Remove a product from user's favorites in Firebase
     *
     * @param userId
     * @param favoriteId
     * @return Task
     */
    fun removeFromFavorites(userId: String, favoriteId: String): Task<Void> {
        return db.collection(USERS_COLLECTION).document(userId)
            .update("favoritesIds", FieldValue.arrayRemove(favoriteId))
    }

    /**
     * Add location to Firebase
     *
     * @param location
     * @return Task
     */
    fun addLocation(location: Location): Task<DocumentReference> {
        return db.collection(LOCATIONS_COLLECTION).add(location)
    }

    /**
     * Add product to Firebase
     *
     * @param product
     * @return Task
     */
    fun addProduct(product: Product): Task<DocumentReference> {
        return db.collection(PRODUCTS_COLLECTION).add(product)
    }

    /**
     * Add image to a certain path to Firebase Storage
     *
     * @param path
     * @param inputStream
     * @return UploadTask
     */
    fun addImage(path: String, inputStream: InputStream): UploadTask {
        val metadata = storageMetadata { contentType = "image/jpg" }
        return storage.reference.child("images/${path}").putStream(inputStream, metadata)
    }

    /**
     * Delete products from Firebase by ids
     *
     * @param ids
     * @return Task
     */
    fun deleteProducts(vararg ids: String): Task<Void> {
        val products = db.batch()

        ids.forEach { id ->
            val reference = db.collection(PRODUCTS_COLLECTION).document(id)
            products.delete(reference)
        }

        return products.commit()
    }

    /**
     * Delete image using image path from Firebase Storage
     *
     * @param path
     * @return Task
     */
    fun deleteImage(path: String): Task<Void> {
        return storage.reference.child("images/${path}").delete()
    }

    /**
     * Delete current user from Firebase
     *
     * @return Task
     */
    fun deleteUser(): Task<Void>? {
        return auth.currentUser?.delete()
    }

    /**
     * Create a query to get discussions using userId from Firebase
     *
     * @param userId
     * @return Query
     */
    fun getDiscussions(userId: String): Query {
        return db.collection(DISCUSSIONS_COLLECTION).whereArrayContains("usersIds", userId)
    }

    /**
     * Add a message to a discussion then update this discussion to Firebase
     *
     * @param id
     * @param message
     * @return Task
     */
    fun updateDiscussion(id: String, message: Message): Task<Void> {
        return db.collection(DISCUSSIONS_COLLECTION).document(id)
            .update("messages", FieldValue.arrayUnion(message), "haveMessages", true)
    }

    /**
     * Add discussion to Firebase
     *
     * @param discussion
     * @return Task
     */
    fun addDiscussion(discussion: Discussion): Task<DocumentReference> {
        return db.collection(DISCUSSIONS_COLLECTION).add(discussion)
    }

    /**
     * Send an email to current user to reset his/her password
     *
     * @param email
     * @return Task
     */
    fun resetPassword(email: String): Task<Void> {
        return auth.sendPasswordResetEmail(email)
    }

    /**
     * Update current user's informations in Firebase
     *
     * @param user
     * @return Task
     */
    fun updateUser(user: User): Task<Void> {
        return db.collection(USERS_COLLECTION).document(user.id!!).update(
            "username",
            user.username,
            "phoneNumber",
            user.phoneNumber,
            "contactByPhone",
            user.contactByPhone,
            "isProfessional",
            user.isProfessional,
            "location",
            user.location,
            "companyName",
            user.companyName,
            "siretNumber",
            user.siretNumber,
            "favoritesIds",
            user.favoritesIds
        )
    }
}