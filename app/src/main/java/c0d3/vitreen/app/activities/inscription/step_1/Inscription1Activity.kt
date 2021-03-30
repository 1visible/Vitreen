package c0d3.vitreen.app.activities.inscription.step_1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.inscription.step_2.Inscription2Activity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Inscription1Activity : AppCompatActivity() {

    private lateinit var nextButton: Button
    private lateinit var anonymousButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_register1)
        email = findViewById<EditText>(R.id.email)
        password = findViewById<EditText>(R.id.password)
        confirmPassword = findViewById<EditText>(R.id.password_confirmation)
        nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.setOnClickListener {
            if ((!(email.text.toString().replace("\\s+", "")
                    .equals(""))) && (!(password.text.toString().replace("\\s+", "")
                    .equals(""))) && (!(confirmPassword.text.toString().replace("\\s+", "")
                    .equals("")))
            ) {

                if (password.text.toString().equals(confirmPassword.text.toString())) {
                    startSignUpActivity()
                } else {
                    Toast.makeText(this, getString(R.string.NoMatchPassword), Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, getString(R.string.emptyFields), Toast.LENGTH_SHORT).show()
            }
        }
        anonymousButton = findViewById<Button>(R.id.anonymous_button)
        anonymousButton.setOnClickListener {
            startAnonymous()
        }
    }

    private fun startAnonymous() {
        Firebase.auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Direction Accueil
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(this, getString(R.string.ErrorMessage), Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun startSignUpActivity() {
        Firebase.auth.createUserWithEmailAndPassword(
            email.text.toString(),
            password.text.toString()
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = Firebase.auth.currentUser
                val goToNextStep = Intent(this, Inscription2Activity::class.java)
                goToNextStep.putExtra(Constants.KEYEMAIL, user.email)
                startActivity(goToNextStep)
            } else {
                Toast.makeText(this, getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()
                //pour l'instant ne redirige nulle part, on va juste afficher un toast disant que l'inscription a échoué
            }
        }
    }

}
