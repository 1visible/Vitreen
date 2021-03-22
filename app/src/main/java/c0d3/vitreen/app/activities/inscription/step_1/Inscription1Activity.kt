package c0d3.vitreen.app.activities.inscription.step_1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import c0d3.vitreen.app.MainActivity
import c0d3.vitreen.app.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Inscription1Activity : AppCompatActivity() {

    private lateinit var nextButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText

    private val KEYNAME = "KEYNAME"
    private val KEYEMAIL = "KEYEMAIL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscription_1)
        email = findViewById<EditText>(R.id.email)
        password = findViewById<EditText>(R.id.password)
        nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.setOnClickListener {
            startSignInActivity()
        }
    }

    fun startSignInActivity() {
        Firebase.auth.createUserWithEmailAndPassword(
            email.text.toString(),
            password.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = Firebase.auth.currentUser
                //Création INTENT afin de partir vers l'étape 2 et transférer user
                Toast.makeText(this, "Connexion Réussie", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Auth failed", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

}