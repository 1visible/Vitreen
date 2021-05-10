package c0d3.vitreen.app.utils

import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.utils.Constants.Companion.CATEGORIES_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.DOCUMENTS_LIMIT
import c0d3.vitreen.app.utils.Constants.Companion.IMAGES_LIMIT_PROFESSIONAL
import c0d3.vitreen.app.utils.Constants.Companion.LOCATIONS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.PRODUCTS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.USERS_COLLECTION
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.InputStream

class FirestoreRepository {
    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth
    private val storage = Firebase.storage
    private var imagesRef: StorageReference = storage.reference.child("images")

    // Get products (filters available)
    fun getProducts(
        limit: Boolean,
        title: String?,
        price: Double?,
        brand: String?,
        location: Location?,
        category: Category?,
        ids: ArrayList<String>?
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

        if (!ids.isNullOrEmpty())
            query = query.whereIn("id", ids)

        if (limit)
            query = query.limit(DOCUMENTS_LIMIT)

        if (price != null)
            query = query.whereLessThanOrEqualTo("price", price)
                .orderBy("price", Query.Direction.ASCENDING)

        return query.orderBy("modifiedAt", Query.Direction.DESCENDING)
    }

    fun getProduct(productId: String): DocumentReference {
        return db.collection(PRODUCTS_COLLECTION).document(productId)
    }

    // Sign in user
    fun signInAnonymously(): Task<AuthResult> {
        return auth.signInAnonymously()
    }

    fun signIn(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun getUser(user: FirebaseUser): Query {
        return db.collection(USERS_COLLECTION).whereEqualTo("emailAddress", user.email).limit(1)
    }

    // Get all categories
    fun getCategories(): CollectionReference {
        return db.collection(CATEGORIES_COLLECTION)
    }

    // Get all locations
    fun getLocations(name: String? = null): Query {
        var query: Query = db.collection(LOCATIONS_COLLECTION)

        if (name != null)
            query = query.whereEqualTo("name", name)

        return query
    }

    fun getImages(productId: String, i: Long): Task<ByteArray> {
        val productImageRef =
            imagesRef.child("${productId}/image_$i")
        val FIVE_MEGABYTE: Long = 1024 * 1024 * 5
        return productImageRef.getBytes(FIVE_MEGABYTE)
    }

    fun updateLocation(locationId: String, zipCode: Long) {
        db.collection(LOCATIONS_COLLECTION)
            .document(locationId)
            .update("zipCode", zipCode)
    }

    fun updateUser(
        userId: String,
        productsIds: ArrayList<String>? = null,
        favoritesProduct: ArrayList<String>? = null
    ) {
        if (productsIds != null) {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update("productsId", productsIds)
        }
        if (favoritesProduct != null) {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update("favoriteProductsId", productsIds)
        }
    }

    fun addLocation(location: Location) {
        db.collection(LOCATIONS_COLLECTION)
            .add(location)
    }

    fun addProduct(product: Product): Task<DocumentReference> {
        return db.collection(PRODUCTS_COLLECTION)
            .add(product)
    }

    fun addImages(productId: String, inputStream: ArrayList<InputStream>) {
        val metadata = storageMetadata { contentType = "image/jpg" }
        for (i in inputStream.indices)
            imagesRef.child("${productId}/image_$i")
                .putStream(inputStream[i], metadata)
    }

    fun deleteProducts(ids: ArrayList<String>): Task<Void> {
        val products = db.batch()

        ids.forEach { id ->
            val reference = db.collection(PRODUCTS_COLLECTION).document(id)
            products.delete(reference)
        }

        return products.commit()
    }

    fun deleteImage(id: String, number: Int): Task<Void> {
        return storage.reference.child("images/${id}/image_$number").delete()
    }

    fun deleteUser(user: FirebaseUser): Task<Void> {
        return user.delete()
    }

    /*
    // save address to firebase
    fun saveAddressItem(addressItem: AddressItem): Task<Void> {
        //var
        var documentReference = db.collection("users").document(user!!.email.toString())
            .collection("saved_addresses").document(addressItem.addressId)
        return documentReference.set(addressItem)
    }

    fun deleteAddress(addressItem: AddressItem): Task<Void> {
        var documentReference =  db.collection("users/${user!!.email.toString()}/saved_addresses")
            .document(addressItem.addressId)

        return documentReference.delete()
    }
    */
}