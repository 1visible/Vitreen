package c0d3.vitreen.app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Product

class FirestoreViewModel : ViewModel(){
    private val repository = FirestoreRepository()
    private var products: MutableLiveData<List<Product>> = MutableLiveData()
    private var signInNotifier: MutableLiveData<Int> = MutableLiveData()

    // Get realtime updates from firebase regarding products
    fun getProducts(): LiveData<List<Product>> {
        repository.getProducts().addSnapshotListener { value, exception ->
            // TODO if (exception != null || value == null)

            val productsList : MutableList<Product> = mutableListOf()
            for (doc in value!!) {
                val product = doc.toObject(Product::class.java)
                productsList.add(product)
            }
            products.value = productsList
        }

        return products
    }

    fun signInAnonymously(): MutableLiveData<Int> {
        repository.signInAnonymously()
            .addOnCompleteListener { task ->
                val errorCode = if(task.isSuccessful) -1 else R.string.errorMessage
                signInNotifier.value = errorCode
            }

        return signInNotifier
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