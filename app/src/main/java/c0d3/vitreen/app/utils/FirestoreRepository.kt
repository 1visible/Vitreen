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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.InputStream

class FirestoreRepository {
    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth
    private val storage = Firebase.storage

    fun getProducts(
        limit: Boolean,
        title: String? = null,
        price: Double? = null,
        brand: String? = null,
        location: Location? = null,
        category: Category? = null,
        ownerId: String? = null,
        ids: ArrayList<String>? = null
    ): Query {
        var query: Query = db.collection(PRODUCTS_COLLECTION)

        if (title != null)
            query = query.whereEqualTo("title", title)

        if (brand != null)
            query = query.whereEqualTo("brand", brand)

        if (location != null)
            query = query.whereEqualTo("location", location)

        if (category != null)
            query = query.whereEqualTo("category", category)

        if (ownerId != null)
            query = query.whereEqualTo("ownerId", ownerId)

        if(!ids.isNullOrEmpty())
            query = query.whereIn("id", ids)

        if (limit)
            query = query.limit(DOCUMENTS_LIMIT)

        if (price != null)
            query = query.whereLessThanOrEqualTo("price", price)
                .orderBy("price", Query.Direction.ASCENDING)

        // TODO query = query.orderBy("modifiedAt", Query.Direction.DESCENDING)

        if(title != null)
            query = query.orderBy("title", Query.Direction.DESCENDING)

        return query
    }

    fun signIn(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun getUser(email: String): Query {
        return db.collection(USERS_COLLECTION).whereEqualTo("emailAddress", email).limit(1)
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

    fun addConsultation(productId: String, consultation: Consultation): Task<Void> {
        return db.collection(PRODUCTS_COLLECTION).document(productId).update("consultations", FieldValue.arrayUnion(consultation))
    }

    fun addToFavorites(userId: String, favoriteId: String): Task<Void> {
        return db.collection(USERS_COLLECTION).document(userId).update("favoritesIds", FieldValue.arrayUnion(favoriteId))
    }

    fun removeFromFavorites(userId: String, favoriteId: String): Task<Void> {
        return db.collection(USERS_COLLECTION).document(userId).update("favoritesIds", FieldValue.arrayRemove(favoriteId))
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

    fun updateDiscussion (id: String, message: Message): Task<Void> {
        return db.collection(DISCUSSIONS_COLLECTION).document(id).update("messages", FieldValue.arrayUnion(message))
    }

    fun addDiscussion(discussion: Discussion): Task<DocumentReference> {
        return db.collection(DISCUSSIONS_COLLECTION).add(discussion)
    }
}