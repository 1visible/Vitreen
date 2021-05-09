package c0d3.vitreen.app.utils

import c0d3.vitreen.app.utils.Constants.Companion.CATEGORIES_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.LOCATIONS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.PRODUCTS_COLLECTION
import c0d3.vitreen.app.utils.Constants.Companion.USERS_COLLECTION
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreRepository {
    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth
    private val user: FirebaseUser? = auth.currentUser

    // Get all products
    fun getProducts(): CollectionReference {
        return db.collection(PRODUCTS_COLLECTION)
    }

    // Sign in user
    fun signInAnonymously(): Task<AuthResult> {
        return auth.signInAnonymously()
    }

    // Get all users
    fun getUsers(): CollectionReference {
        return db.collection(USERS_COLLECTION)
    }

    // Get all categories
    fun getCategories(): CollectionReference {
        return db.collection(CATEGORIES_COLLECTION)
    }

    // Get all locations
    fun getLocations(): CollectionReference {
        return db.collection(LOCATIONS_COLLECTION)
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