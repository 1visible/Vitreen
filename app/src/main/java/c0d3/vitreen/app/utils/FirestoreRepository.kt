package c0d3.vitreen.app.utils

import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.utils.Constants.Companion.CATEGORIES_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.DOCUMENTS_LIMIT
import c0d3.vitreen.app.utils.Constants.Companion.LOCATIONS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.PRODUCTS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.USERS_COLLECTION
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreRepository {
    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth
    private val user: FirebaseUser? = auth.currentUser

    // Get products (filters available)
    fun getProducts(
        limit: Boolean,
        title: String?,
        price: Double?,
        brand: String?,
        location: Location?,
        category: Category?
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

        if (limit)
            query = query.limit(DOCUMENTS_LIMIT)

        if (price != null)
            query = query.whereLessThanOrEqualTo("price", price)
                .orderBy("price", Query.Direction.ASCENDING)

        return query.orderBy("modifiedAt", Query.Direction.DESCENDING)
    }

    // Sign in user
    fun signInAnonymously(): Task<AuthResult> {
        return auth.signInAnonymously()
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
        if (name != null) {
            query = query.whereEqualTo("name", name)
        }
        return query
    }

    fun updateLocation(city: String, zipCode: Long) {
        db.collection(LOCATIONS_COLLECTION)
            .whereEqualTo("name", city)
            .get()
            .addOnSuccessListener { locations ->
                if (locations.size() == 1) {
                    locations.forEach { location ->
                        db.collection(LOCATIONS_COLLECTION)
                            .document(location.id)
                            .update("zipCode", zipCode)
                    }
                }
            }
    }

    fun addLocation(location: Location) {
        db.collection(LOCATIONS_COLLECTION)
            .add(location)
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