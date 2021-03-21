package c0d3.vitreen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import c0d3.vitreen.app.models.Users
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    val TAG: String = "c0d3"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Access a Cloud Firestore instance from your Activity
        val db = Firebase.firestore

        //test add data
        val listAnnonce: ArrayList<String> = ArrayList()
        listAnnonce.add("t")
        listAnnonce.add("v")
        listAnnonce.add("v")
        val user = Users(
            "ADOLPHE",
            "Benjamin",
            "adolphe906@gmail.com",
            "aeiouy",
            "dada",
            null,
            null,
            "0630058952",
            "mail",
            listAnnonce,
            listAnnonce
        )
        db.collection("users").add(user).addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
}