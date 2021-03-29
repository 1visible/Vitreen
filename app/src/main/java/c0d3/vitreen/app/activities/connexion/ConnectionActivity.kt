package c0d3.vitreen.app.activities.connexion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ConnectionActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var connexionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        email = findViewById<EditText>(R.id.email)
        password = findViewById<EditText>(R.id.password)
        connexionButton = findViewById<Button>(R.id.connexionButton)
        connexionButton.setOnClickListener {
            if ((!(email.text.toString().equals(""))) && (!(password.text.toString().equals("")))) {
                signInUser()
            } else {
                Toast.makeText(this, getString(R.string.emptyFields), Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun signInUser() {
        Firebase.auth
            .signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(Constants.TAG, getString(R.string.SignInSucceed))
                    Toast.makeText(this, getString(R.string.SignInSucceed), Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Log.w(Constants.TAG, "Auth failed")
                    Toast.makeText(this, getString(R.string.ErrorMessage), Toast.LENGTH_SHORT)
                        .show()
                    email.text.clear()
                    password.text.clear()
                }
            }
    }
}