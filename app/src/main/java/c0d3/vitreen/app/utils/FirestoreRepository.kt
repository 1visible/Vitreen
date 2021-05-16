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
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class FirestoreRepository {
    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth
    private val storage = Firebase.storage

    fun getProducts(
        limit: Boolean,
        title: String? = null,
        priceMin: Double? = null,
        priceMax: Double? = null,
        brand: String? = null,
        location: Location? = null,
        category: Category? = null,
        ownerId: String? = null,
        ids: ArrayList<String>? = null
    ): Query {
        var query: Query = db.collection(PRODUCTS_COLLECTION)
        var orderByDate = true

        if (title != null) {
            query = query.whereGreaterThanOrEqualTo("title", title)
                .orderBy("title")
            orderByDate = false
        }

        if (brand != null) {
            query = query.whereEqualTo("brand", brand)
            orderByDate = false
        }

        if (location != null) {
            query = query.whereEqualTo("location", location)
            orderByDate = true
        }

        if (category != null) {
            query = query.whereEqualTo("category", category)
            orderByDate = false
        }

        if (ownerId != null) {
            query = query.whereEqualTo("ownerId", ownerId)
            orderByDate = false
        }

        if (priceMin != null && priceMax != null) {
            query =
                query.orderBy("price", Query.Direction.ASCENDING).startAt(priceMin).endAt(priceMax)
            orderByDate = false
        } else if (priceMin != null) {
            query = query.whereGreaterThanOrEqualTo("price", priceMin)
                .orderBy("price", Query.Direction.ASCENDING)
            orderByDate = false
        } else if (priceMax != null) {
            query = query.whereLessThanOrEqualTo("price", priceMax)
                .orderBy("price", Query.Direction.ASCENDING)
            orderByDate = false
        }

        if (!ids.isNullOrEmpty()) {
            query = query.whereIn(FieldPath.documentId(), ids)
            orderByDate = false
        }

        if (orderByDate)
            query = query.orderBy("modifiedAt", Query.Direction.DESCENDING)

        if (limit)
            query = query.limit(DOCUMENTS_LIMIT)

        return query
    }

    fun deleteProduct(id: String): Task<Void> {
        return db.collection(PRODUCTS_COLLECTION).document(id).delete()
    }

    fun signIn(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun signOut() {
        auth.signOut()
    }

    fun getUser(email: String): Query {
        return db.collection(USERS_COLLECTION).whereEqualTo("emailAddress", email).limit(1)
    }

    fun getUserById(id: String): Task<DocumentSnapshot> {
        return db.collection(USERS_COLLECTION).document(id).get()
    }

    fun reportProduct(id: String, userId: String): Task<Void> {
        return db.collection(PRODUCTS_COLLECTION).document(id)
            .update("reporters", FieldValue.arrayUnion(userId))
    }

    fun registerUser(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun addUser(user: User): Task<DocumentReference> {
        return db.collection(USERS_COLLECTION).add(user)
    }

    fun getCategories(): Query {
        return db.collection(CATEGORIES_COLLECTION)
    }

    fun getLocations(): Query {
        return db.collection(LOCATIONS_COLLECTION)
    }

    fun getImage(path: String): Task<ByteArray> {
        return storage.reference.child("images/${path}").getBytes(IMAGE_SIZE)
    }

    fun updateLocation(id: String, zipCode: Long): Task<Void> {
        return db.collection(LOCATIONS_COLLECTION).document(id).update("zipCode", zipCode)
    }

    fun updateUserLocation(id: String, zipCode: Long): Task<Void> {
        return db.collection(USERS_COLLECTION).document(id).update("location.zipCode", zipCode)
    }


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

    fun addConsultation(productId: String, consultation: Consultation): Task<Void> {
        return db.collection(PRODUCTS_COLLECTION).document(productId)
            .update("consultations", FieldValue.arrayUnion(consultation))
    }

    fun addToFavorites(userId: String, favoriteId: String): Task<Void> {
        return db.collection(USERS_COLLECTION).document(userId)
            .update("favoritesIds", FieldValue.arrayUnion(favoriteId))
    }

    fun removeFromFavorites(userId: String, favoriteId: String): Task<Void> {
        return db.collection(USERS_COLLECTION).document(userId)
            .update("favoritesIds", FieldValue.arrayRemove(favoriteId))
    }

    fun addLocation(location: Location): Task<DocumentReference> {
        return db.collection(LOCATIONS_COLLECTION).add(location)
    }

    fun addProduct(product: Product): Task<DocumentReference> {
        return db.collection(PRODUCTS_COLLECTION).add(product)
    }

    fun addImage(path: String, inputStream: InputStream): UploadTask {
        val metadata = storageMetadata { contentType = "image/jpg" }
        return storage.reference.child("images/${path}").putStream(inputStream, metadata)
    }

    fun deleteProducts(vararg ids: String): Task<Void> {
        val products = db.batch()

        ids.forEach { id ->
            val reference = db.collection(PRODUCTS_COLLECTION).document(id)
            products.delete(reference)
        }

        return products.commit()
    }

    fun deleteImage(path: String): Task<Void> {
        return storage.reference.child("images/${path}").delete()
    }

    fun deleteUser(): Task<Void>? {
        return auth.currentUser?.delete()
    }

    fun getDiscussions(userId: String): Query {
        return db.collection(DISCUSSIONS_COLLECTION).whereArrayContains("usersIds", userId)
    }

    fun updateDiscussion(id: String, message: Message): Task<Void> {
        return db.collection(DISCUSSIONS_COLLECTION).document(id)
            .update("messages", FieldValue.arrayUnion(message))
    }

    fun addDiscussion(discussion: Discussion): Task<DocumentReference> {
        return db.collection(DISCUSSIONS_COLLECTION).add(discussion)
    }
}