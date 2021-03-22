package c0d3.vitreen.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.activities.inscription.step_1.Inscription1Activity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import io.grpc.okhttp.internal.framed.ErrorCode
import kotlin.math.E

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
            startActivity(Intent(this, Inscription1Activity::class.java))
        }
    }


}