package c0d3.vitreen.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.activities.adverts.drop.DropAdvertActivity
import c0d3.vitreen.app.activities.connexion.ConnectionActivity
import c0d3.vitreen.app.activities.inscription.step_1.Inscription1Activity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var inscriptionButton: Button
    private lateinit var connexionButton: Button
    private lateinit var addAdvertButton: Button

    private val auth = Firebase.auth
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inscriptionButton = findViewById<Button>(R.id.InscriptionButton)
        connexionButton = findViewById<Button>(R.id.signInButton)
        addAdvertButton = findViewById<Button>(R.id.addProductButton)
        if (user == null) {
            inscriptionButton.setOnClickListener {
                startActivity(Intent(this, Inscription1Activity::class.java))
            }
            connexionButton.text = getString(R.string.connexion)
            connexionButton.setOnClickListener {
                startActivity(Intent(this, ConnectionActivity::class.java))
            }
            addAdvertButton.visibility = View.GONE
        } else {
            connexionButton.text = getString(R.string.logout)
            connexionButton.setOnClickListener {
                auth.signOut()
                finish()
                startActivity(intent)
            }
            addAdvertButton.visibility = View.VISIBLE
            addAdvertButton.setOnClickListener {
                startActivity(Intent(this, DropAdvertActivity::class.java))
            }
        }
    }
}