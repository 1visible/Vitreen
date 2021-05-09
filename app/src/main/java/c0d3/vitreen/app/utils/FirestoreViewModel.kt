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

class FirestoreViewModel : ViewModel(){
    private val repository = FirestoreRepository()
    private var productsLiveData: MutableLiveData<Pair<Int, List<Product>>> = MutableLiveData()
    private var signInErrorCode: MutableLiveData<Int> = MutableLiveData()
    private var userLiveData: MutableLiveData<Pair<Int, User>> = MutableLiveData()

    // Get realtime updates from firebase regarding products
    fun getProducts(limit: Boolean = true, title: String? = null, price: Double? = null, brand: String? = null, location: Location? = null, category: Category? = null): LiveData<Pair<Int, List<Product>>> {
        repository.getProducts(limit, title, price, brand, location, category).addSnapshotListener { products, exception ->
            val errorCode = if(exception == null) -1 else R.string.network_error
            val productsList: MutableList<Product> = mutableListOf()

            if(products != null) {
                for (document in products) {
                    val product = document.toObject(Product::class.java)
                    productsList.add(product)
                }
            }

            productsLiveData.value = Pair(errorCode, productsList)
        }

        return productsLiveData
    }

    fun signInAnonymously(): MutableLiveData<Int> {
        repository.signInAnonymously()
            .addOnCompleteListener { task ->
                val errorCode = if(task.isSuccessful) -1 else R.string.network_error
                signInErrorCode.value = errorCode
            }

        return signInErrorCode
    }

    // Get realtime updates from firebase regarding user
    fun getUser(user: FirebaseUser): LiveData<Pair<Int, User>> {
        repository.getUser(user).addSnapshotListener { users, exception ->
            var errorCode = if(exception == null) -1 else R.string.network_error

            if(users == null || users.isEmpty)
                errorCode = R.string.errorMessage // TODO : Remplacer par un meilleur message

            val userData = if(users == null || users.isEmpty) User() else users.first().toObject(User::class.java)

            userLiveData.value = Pair(errorCode, userData)
        }

        return userLiveData
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