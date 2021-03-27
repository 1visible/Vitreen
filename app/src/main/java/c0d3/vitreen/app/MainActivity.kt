package c0d3.vitreen.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.activities.connexion.ConnectionActivity
import c0d3.vitreen.app.activities.inscription.step_1.Inscription1Activity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
                startActivity(Intent(this, ConnectionActivity::class.java))
            }
        }
    }
}