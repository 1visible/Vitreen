package c0d3.vitreen.app.activities.adverts.drop.step1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DropAdvert2Activity : AppCompatActivity() {

    private val DB = Firebase.firestore
    private val categories = DB.collection("Categories")
    private val locations = DB.collection("locations")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drop_advert_2)
    }

//    private fun getCategoryId(): String {
//        var res = ""
//        categories
//            .whereEqualTo("name", category.editText?.text.toString())
//            .get()
//            .addOnSuccessListener { documents ->
//                if (documents.size() == 1) {
//                    for (document in documents) {
//                        res = document.id
//                    }
//                }
//            }
//        return res
//    }
//
//    private fun getLocationId() {
//        locations
//            .whereEqualTo("name", location.text.toString())
//            .get()
//            .addOnSuccessListener { documents ->
//                if (documents.size() == 1) {
//                    for (document in documents) {
//                        categoryId = document.id
//                    }
//                } else {
//                    Toast.makeText(this, "size > 0", Toast.LENGTH_SHORT).show()
//                }
//            }.addOnFailureListener {
//                Toast.makeText(this, "problème location", Toast.LENGTH_SHORT).show()
//            }
//    }
}