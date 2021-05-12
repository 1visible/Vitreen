package c0d3.vitreen.app.utils

import c0d3.vitreen.app.models.*
import c0d3.vitreen.app.utils.Constants.Companion.CATEGORIES_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.DOCUMENTS_LIMIT
import c0d3.vitreen.app.utils.Constants.Companion.IMAGE_SIZE
import c0d3.vitreen.app.utils.Constants.Companion.LOCATIONS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.PRODUCTS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.USERS_COLLECTION
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
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
        title: String?,
        price: Double?,
        brand: String?,
        location: Location?,
        category: Category?,
        ownerId: String?
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

        if (limit)
            query = query.limit(DOCUMENTS_LIMIT)

        if (price != null)
            query = query.whereLessThanOrEqualTo("price", price)
                .orderBy("price", Query.Direction.ASCENDING)

        query = query.orderBy("modifiedAt", Query.Direction.DESCENDING)

        if(title != null)
            query = query.orderBy("title", Query.Direction.DESCENDING)

        return query
    }

    fun getProduct(id: String): Task<DocumentSnapshot> {
        return db.collection(PRODUCTS_COLLECTION).document(id).get()
    }

    fun signInAnonymously(): Task<AuthResult> {
        return auth.signInAnonymously()
    }

    fun signIn(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun getUser(user: FirebaseUser): Task<QuerySnapshot> {
        return db.collection(USERS_COLLECTION).whereEqualTo("emailAddress", user.email).limit(1).get()
    }

    fun linkUser(user: FirebaseUser, email: String, password: String): Task<AuthResult> {
        val credential = EmailAuthProvider.getCredential(email, password)
        return user.linkWithCredential(credential)
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

    fun getLocation(city: String): Task<QuerySnapshot> {
        return getLocations().whereEqualTo("city", city).get()
    }

    fun getImage(path: String): Task<ByteArray> {
        return storage.reference.child("images/${path}").getBytes(IMAGE_SIZE)
    }

    fun updateLocation(id: String, zipCode: Long): Task<Void> {
        return db.collection(LOCATIONS_COLLECTION).document(id).update("zipCode", zipCode)
    }

    fun updateProduct(id: String, consultation: Consultation): Task<Void> {
        return db.collection(PRODUCTS_COLLECTION).document(id).update("consultations", FieldValue.arrayUnion(consultation))
    }

    fun updateUser(id: String, favoriteId: String): Task<Void> {
        return db.collection(USERS_COLLECTION).document(id).update("favoritesIds", FieldValue.arrayUnion(favoriteId))
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

    fun deleteProducts(ids: ArrayList<String>): Task<Void> {
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

    fun deleteUser(user: FirebaseUser): Task<Void> {
        return user.delete()
    }
}