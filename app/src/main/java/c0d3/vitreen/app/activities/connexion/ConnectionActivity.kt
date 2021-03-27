package c0d3.vitreen.app.activities.connexion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.Constantes
import c0d3.vitreen.app.MainActivity
import c0d3.vitreen.app.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.math.sign

class ConnectionActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var connexionButton: Button
    private lateinit var errorMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        email = findViewById<EditText>(R.id.email)
        password = findViewById<EditText>(R.id.password)
        errorMessage = findViewById<EditText>(R.id.errorMessage)
        connexionButton = findViewById<Button>(R.id.connexionButton)
        connexionButton.setOnClickListener {
            signInUser()
        }

    }

    private fun signInUser() {
        Firebase.auth
            .signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(Constantes.TAG, "Auth Succeed")
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Log.w(Constantes.TAG, "Auth failed")
                    Toast.makeText(this, "Auth failed", Toast.LENGTH_SHORT).show()
                    email.text.clear()
                    password.text.clear()
                    errorMessage.text = getString(R.string.errorMessage)
                }
            }
    }
}