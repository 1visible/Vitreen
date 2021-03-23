package c0d3.vitreen.app.activities.inscription.step_1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.MainActivity
import c0d3.vitreen.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Inscription1Activity : AppCompatActivity() {

    val TAG: String = "c0d3"

    private lateinit var nextButton: Button
    private lateinit var anonymousButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscription_1)
        email = findViewById<EditText>(R.id.email)
        password = findViewById<EditText>(R.id.password)
        nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.setOnClickListener {
            startSignInActivity()
        }
        anonymousButton = findViewById<Button>(R.id.anonymousButton)
        anonymousButton.setOnClickListener {
            startAnonymous()
        }
    }

    private fun startAnonymous() {
        Firebase.auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Direction Accueil
                } else {
                    Toast.makeText(this, "Auth failed", Toast.LENGTH_SHORT).show()
                    //pour l'instant ne redirige nulle part, on va juste afficher un toast disant que l'inscription a échoué
                }
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
                //pour l'instant ne redirige nulle part, on va juste afficher un toast disant que l'inscription a échoué
            }
        }
    }

}