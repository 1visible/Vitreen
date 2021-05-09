package c0d3.vitreen.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Product

class FirestoreViewModel : ViewModel(){
    private val repository = FirestoreRepository()
    private var productsLiveData: MutableLiveData<Pair<Int, List<Product>>> = MutableLiveData()
    private var signInErrorCode: MutableLiveData<Int> = MutableLiveData()

    // Get realtime updates from firebase regarding products
    fun getProducts(): LiveData<Pair<Int, List<Product>>> {
        repository.getProducts().addSnapshotListener { products, exception ->
            val errorCode = if(exception == null) -1 else R.string.network_error
            val productsList : MutableList<Product> = mutableListOf()

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