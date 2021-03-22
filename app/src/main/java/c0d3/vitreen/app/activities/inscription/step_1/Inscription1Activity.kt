package c0d3.vitreen.app.activities.inscription.step_1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    private val KEYNAME = "KEYNAME"
    private val KEYEMAIL = "KEYEMAIL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startSignInActivity()
    }

    fun startSignInActivity() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), MainActivity.RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.handleResponseAfterSignIn(requestCode, resultCode, data)
    }

    private fun handleResponseAfterSignIn(requestCode: Int, resultCode: Int, data: Intent?) {
        val response: IdpResponse = IdpResponse.fromResultIntent(data)!!
        if (requestCode == MainActivity.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Connexion OK", Toast.LENGTH_SHORT).show()
                var user = Firebase.auth.currentUser
                if (user != null) {
                    //Création de l'instance et passage des variables du user
                }

            } else {
                if (response == null) {
                    Toast.makeText(this, "Connexion annulée", Toast.LENGTH_SHORT).show()
                } else if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "pas de wifi", Toast.LENGTH_SHORT).show()
                } else if (response.error?.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}