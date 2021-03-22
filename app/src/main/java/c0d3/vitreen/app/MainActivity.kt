package c0d3.vitreen.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.activities.inscription.step_1.Inscription1Activity
import com.firebase.ui.auth.AuthUI

class MainActivity : AppCompatActivity() {
    val TAG: String = "c0d3"
    private lateinit var inscriptionButton: Button

    companion object {
        val RC_SIGN_IN: Int = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inscriptionButton = findViewById<Button>(R.id.InscriptionButton)
        inscriptionButton.setOnClickListener {
            startSignInActivity()
        }
    }

    fun startSignInActivity() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), RC_SIGN_IN
        )
    }
}