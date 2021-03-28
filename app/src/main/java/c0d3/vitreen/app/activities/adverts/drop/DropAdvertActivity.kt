package c0d3.vitreen.app.activities.adverts.drop

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.MainActivity
import c0d3.vitreen.app.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DropAdvertActivity : AppCompatActivity() {

    private lateinit var category: TextInputLayout
    private val user = Firebase.auth.currentUser

    override fun onStart() {
        super.onStart()
        if (user == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drop_advert)
    }
}