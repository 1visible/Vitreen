package c0d3.vitreen.app.activities.adverts.drop.step2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.Constantes
import c0d3.vitreen.app.MainActivity
import c0d3.vitreen.app.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DropAdvert2Activity : AppCompatActivity() {

    private val user = Firebase.auth.currentUser
    private val DB = Firebase.firestore
    private val categories = DB.collection("Categories")
    private val locations = DB.collection("locations")

    private lateinit var category: String
    private lateinit var title: String
    private lateinit var price: String
    private lateinit var location: String
    private lateinit var description: String

    override fun onStart() {
        super.onStart()

        if ((user == null)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        if ((user != null) && user.isAnonymous) {
            //redirige l'utilisateur vers une inscription non anonyme
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drop_advert_2)
        if (intent != null) {
            category = intent.getStringExtra(Constantes.KEYADDADVERTS[0]).toString()
            title = intent.getStringExtra(Constantes.KEYADDADVERTS[1]).toString()
            price = intent.getStringExtra(Constantes.KEYADDADVERTS[2]).toString()
            location = intent.getStringExtra(Constantes.KEYADDADVERTS[3]).toString()
            description = intent.getStringExtra(Constantes.KEYADDADVERTS[4]).toString()
        }
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