package c0d3.vitreen.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.activities.connexion.ConnexionActivity
import c0d3.vitreen.app.activities.inscription.step_1.Inscription1Activity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.grpc.okhttp.internal.framed.ErrorCode
import kotlin.math.E

class MainActivity : AppCompatActivity() {
    private lateinit var inscriptionButton: Button
    private lateinit var connexionButton: Button

    private val auth = Firebase.auth
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inscriptionButton = findViewById<Button>(R.id.InscriptionButton)
        inscriptionButton.setOnClickListener {
            if (user == null) startActivity(Intent(this, Inscription1Activity::class.java))
        }

        connexionButton = findViewById<Button>(R.id.signInButton)
        if (user == null) {
            connexionButton.text = R.string.connexion.toString()
            connexionButton.setOnClickListener {
                startActivity(Intent(this, ConnexionActivity::class.java))
            }
        }
    }
}