package c0d3.vitreen.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Category
import c0d3.vitreen.app.models.Location
import c0d3.vitreen.app.models.Product
import c0d3.vitreen.app.models.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Query

class FirestoreViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private var productsLiveData: MutableLiveData<Pair<Int, List<Product>>> = MutableLiveData()
    private var signInErrorCode: MutableLiveData<Int> = MutableLiveData()
    private var userLiveData: MutableLiveData<Pair<Int, User>> = MutableLiveData()
    private var categoriesLiveData: MutableLiveData<Pair<Int, List<Category>>> = MutableLiveData()
    private var locationsLiveData: MutableLiveData<Pair<Int, List<Location>>> = MutableLiveData()

    // Get realtime updates from firebase regarding products
    fun getProducts(limit: Boolean = true, title: String? = null, price: Double? = null, brand: String? = null, location: Location? = null, category: Category? = null): LiveData<Pair<Int, List<Product>>> {
        return getList(repository.getProducts(limit, title, price, brand, location, category), productsLiveData)
    }

    // Get realtime updates from firebase regarding sign in (anonymously)
    fun signInAnonymously(): MutableLiveData<Int> {
        repository.signInAnonymously()
            .addOnCompleteListener { task ->
                val errorCode = if (task.isSuccessful) -1 else R.string.network_error
                signInErrorCode.value = errorCode
            }

        return signInErrorCode
    }

    // Get realtime updates from firebase regarding user
    fun getUser(user: FirebaseUser): LiveData<Pair<Int, User>> {
        repository.getUser(user).addSnapshotListener { users, exception ->
            var errorCode = if(exception == null) -1 else R.string.network_error
            val userData: User

            if(users == null || users.isEmpty) {
                errorCode = R.string.errorMessage // TODO : Remplacer par un meilleur message
                userData = User()
            } else
                userData = users.first().toObject(User::class.java)

            userLiveData.value = Pair(errorCode, userData)
        }

        return userLiveData
    }

    // Get realtime updates from firebase regarding categories
    fun getCategories(): LiveData<Pair<Int, List<Category>>> {
        return getList(repository.getCategories(), categoriesLiveData)
    }

    // Get realtime updates from firebase regarding categories
    fun getLocations(): LiveData<Pair<Int, List<Location>>> {
        return getList(repository.getLocations(), locationsLiveData)
    }

    private inline fun <reified T> getList(query: Query, liveData: MutableLiveData<Pair<Int, List<T>>>): LiveData<Pair<Int, List<T>>> {
        query.addSnapshotListener { documents, exception ->
            val errorCode = if(exception == null) -1 else R.string.network_error
            val valuesList: MutableList<T> = mutableListOf()

            if(documents != null) {
                for (document in documents) {
                    val value = document.toObject(T::class.java)
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