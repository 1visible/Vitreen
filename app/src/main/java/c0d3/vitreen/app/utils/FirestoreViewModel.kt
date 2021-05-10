package c0d3.vitreen.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query

class FirestoreViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private var errorCodeLiveData: MutableLiveData<Int> = MutableLiveData()
    private var productsLiveData: MutableLiveData<Pair<Int, List<Product>>> = MutableLiveData()
    private var userLiveData: MutableLiveData<Pair<Int, User>> = MutableLiveData()
    private var locationLiveData: MutableLiveData<Pair<Int, Location>> = MutableLiveData()
    var categoriesLiveData: MutableLiveData<Pair<Int, List<Category>>> = MutableLiveData()
    var locationsLiveData: MutableLiveData<Pair<Int, List<Location>>> = MutableLiveData()

    fun getProducts(
        limit: Boolean = true,
        title: String? = null,
        price: Double? = null,
        brand: String? = null,
        location: Location? = null,
        category: Category? = null,
        ids: ArrayList<String>? = null
    ): LiveData<Pair<Int, List<Product>>> {
        return getList(repository.getProducts(limit, title, price, brand, location, category, ids), productsLiveData)
    }

    fun signInAnonymously(): MutableLiveData<Int> {
        repository.signInAnonymously()
            .addOnCompleteListener { task ->
                val errorCode = if (task.isSuccessful) -1 else R.string.network_error
                errorCodeLiveData.value = errorCode
            }

        return errorCodeLiveData
    }

    fun getUser(user: FirebaseUser): LiveData<Pair<Int, User>> {
        repository.getUser(user).addSnapshotListener { users, exception ->
            var errorCode = if (exception == null) -1 else R.string.network_error
            val userData: User

            if (users == null || users.isEmpty) {
                errorCode = R.string.errorMessage // TODO : Remplacer par un meilleur message
                userData = User()
            } else
                userData = users.first().toObject(User::class.java)

            userLiveData.value = Pair(errorCode, userData)
        }

        return userLiveData
    }

    fun getCategories(): LiveData<Pair<Int, List<Category>>> {
        return getList(repository.getCategories(), categoriesLiveData)
    }

    fun getLocations(): LiveData<Pair<Int, List<Location>>> {
        return getList(repository.getLocations(), locationsLiveData)
    }

    fun getLocation(name: String?): LiveData<Pair<Int, Location>> {
        repository.getLocations(name).addSnapshotListener { locations, exception ->
            var errorCode = if (exception == null) -1 else R.string.network_error
            val locationData: Location

            if (locations == null || locations.isEmpty) {
                errorCode = R.string.errorMessage // TODO : Remplacer par un meilleur message
                locationData = Location()
            } else
                locationData = locations.first().toObject(Location::class.java)

            locationLiveData.value = Pair(errorCode, locationData)
        }

        return locationLiveData
    }

    fun updateLocation(city: String, zipCode: Long) {
        repository.updateLocation(city, zipCode)
    }

    fun addLocation(location: Location) {
        repository.addLocation(location)
    }

    fun deleteProducts(ids: ArrayList<String>) {
        repository.deleteProducts(ids).addOnCompleteListener { task ->
            val errorCode = if (task.isSuccessful) -1 else R.string.network_error
            errorCodeLiveData.value = errorCode
        }
    }

    private inline fun <reified T: Entity> getList(query: Query, liveData: MutableLiveData<Pair<Int, List<T>>>): LiveData<Pair<Int, List<T>>> {
        query.addSnapshotListener { documents, exception ->
            val errorCode = if (exception == null) -1 else R.string.network_error
            val valuesList: MutableList<T> = mutableListOf()

            if (documents != null) {
                for (document in documents) {
                    val value = document.toObject(T::class.java)
                    value.id = document.id
                    valuesList.add(value)
                }
            }

            liveData.value = Pair(errorCode, valuesList)
        }

        return liveData
    }

    /*
    // save address to firebase
    fun saveAddressToFirebase(addressItem: AddressItem){
        repository.saveAddressItem(addressItem).addOnFailureListener {
            Log.e(TAG,"Failed to save Address!")
        }
    }

    // delete an address from firebase
    fun deleteAddress(addressItem: AddressItem){
        repository.deleteAddress(addressItem).addOnFailureListener {
            Log.e(TAG,"Failed to delete Address")
        }
    }
    */
}