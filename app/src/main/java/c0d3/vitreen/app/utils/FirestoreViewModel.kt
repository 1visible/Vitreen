package c0d3.vitreen.app.utils

import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.*
import c0d3.vitreen.app.utils.Constants.Companion.IMAGES_LIMIT_PROFESSIONAL
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.InputStream

class FirestoreViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private var errorCodeLiveData: MutableLiveData<Int> = MutableLiveData()
    private var productsLiveData: MutableLiveData<Pair<Int, List<Product>>> = MutableLiveData()
    private var userLiveData: MutableLiveData<Pair<Int, User>> = MutableLiveData()
    private var locationLiveData: MutableLiveData<Pair<Int, Location>> = MutableLiveData()
    var categoriesLiveData: MutableLiveData<Pair<Int, List<Category>>> = MutableLiveData()
    var locationsLiveData: MutableLiveData<Pair<Int, List<Location>>> = MutableLiveData()


    private val storage = Firebase.storage
    private var imagesRef: StorageReference = storage.reference.child("images")

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

    fun signInAnonymously(): LiveData<Int> {
        repository.signInAnonymously().addOnCompleteListener { task ->
            val errorCode = if (task.isSuccessful) -1 else R.string.network_error
            errorCodeLiveData.value = errorCode
        }

        return errorCodeLiveData
    }

    fun signIn(email: String, password: String): LiveData<Int> {
        repository.signIn(email, password).addOnCompleteListener { task ->
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
            } else {
                userData = toObject(users.first(), User::class.java)
            }

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
                locationData = toObject(locations.first(), Location::class.java)

            locationLiveData.value = Pair(errorCode, locationData)
        }

        return locationLiveData
    }

    fun updateLocation(locationId:String, zipCode: Long) {
        repository.updateLocation(locationId,zipCode)
    }

    fun updateUser(userId:String,productsId:ArrayList<String>){
        repository.updateUser(userId,productsId)
    }

    fun addLocation(location: Location) {
        repository.addLocation(location)
    }

    fun addProduct(product: Product, inputStream:ArrayList<InputStream>,user:User):LiveData<Int>{
        repository.addProduct(product)
            .addOnCompleteListener { task->
                val errorCode = if(task.isSuccessful) -1 else R.string.network_error
                errorCodeLiveData.value = errorCode
                val metadata = storageMetadata { contentType = "image/jpg" }

                for (i in inputStream.indices)
                    imagesRef.child("${product.id}/image_$i")
                        .putStream(inputStream[i], metadata)

                user.productsId.add(product.id)
                updateUser(user.id,user.productsId)
            }
        return errorCodeLiveData
    }

    fun deleteProducts(ids: ArrayList<String>): LiveData<Int> {
        deleteImages(ids)
        repository.deleteProducts(ids).addOnCompleteListener { task ->
            val errorCode = if (task.isSuccessful) -1 else R.string.network_error
            errorCodeLiveData.value = errorCode
        }

        return errorCodeLiveData
    }

    private fun deleteImages(ids: ArrayList<String>) {
        ids.forEach { id ->
            for(number in 0..IMAGES_LIMIT_PROFESSIONAL)
                repository.deleteImage(id, number)
        }
    }

    fun deleteUser(user: FirebaseUser): LiveData<Int> {
        repository.deleteUser(user).addOnCompleteListener { task ->
            val errorCode = if (task.isSuccessful) -1 else R.string.network_error
            errorCodeLiveData.value = errorCode
        }

        return errorCodeLiveData
    }

    private inline fun <reified T: Entity> getList(query: Query, liveData: MutableLiveData<Pair<Int, List<T>>>): LiveData<Pair<Int, List<T>>> {
        query.addSnapshotListener { documents, exception ->
            val errorCode = if (exception == null) -1 else R.string.network_error
            val valuesList: MutableList<T> = mutableListOf()

            if (documents != null) {
                for (document in documents) {
                    val value = toObject(document, T::class.java)
                    valuesList.add(value)
                }
            }

            liveData.value = Pair(errorCode, valuesList)
        }

        return liveData
    }

    private fun <T: Entity> toObject(document: QueryDocumentSnapshot, @NonNull type: Class<T>): T {
        val obj = document.toObject(type)
        obj.id = document.id

        return obj
    }
}