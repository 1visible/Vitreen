package c0d3.vitreen.app.activities.inscription.step_1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import c0d3.vitreen.app.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Inscription1Activity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inscription1)
        // Access a Cloud Firestore instance from your Activity
        val db = Firebase.firestore
    }
}