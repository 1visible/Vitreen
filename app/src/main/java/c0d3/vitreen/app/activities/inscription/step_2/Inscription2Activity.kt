package c0d3.vitreen.app.activities.inscription.step_2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.utils.Constants
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.User
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Inscription2Activity : AppCompatActivity() {

    private lateinit var email: String

    private lateinit var lastName: EditText
    private lateinit var firstName: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var contactMethod: EditText
    private lateinit var companyName: EditText
    private lateinit var siret: EditText
    private lateinit var switch: SwitchMaterial
    private lateinit var submitButton: Button

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscription_2)
        if (intent != null) {
            email = intent.getStringExtra(Constants.KEYEMAIL).toString()
            lastName = findViewById<EditText>(R.id.lastName)
            firstName = findViewById<EditText>(R.id.firstName)
            phoneNumber = findViewById<EditText>(R.id.phoneNumber)
            contactMethod = findViewById<EditText>(R.id.contactMethod)
            companyName = findViewById<EditText>(R.id.companyName)
            siret = findViewById<EditText>(R.id.siret)
            switch = findViewById<SwitchMaterial>(R.id.switchPro)
            switch.isChecked = false
            switch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    companyName.visibility = View.VISIBLE
                    siret.visibility = View.VISIBLE
                } else {
                    companyName.visibility = View.GONE
                    siret.visibility = View.GONE
                }
            }
            submitButton = findViewById<Button>(R.id.submitButton)
            submitButton.setOnClickListener { _ ->
                val user: User
                if (switch.isChecked) {
                    user = User(
                        lastName.text.toString(),
                        firstName.text.toString(),
                        email,
                        switch.isChecked,
                        companyName.text.toString(),
                        siret.text.toString(),
                        phoneNumber.text.toString(),
                        contactMethod.text.toString(),
                        null,
                        null
                    )
                } else {
                    user = User(
                        lastName = lastName.text.toString(),
                        firstName = firstName.text.toString(),
                        email = email,
                        isProfessional = switch.isChecked,
                        phone = phoneNumber.text.toString(),
                        contactMethod = contactMethod.text.toString()
                    )
                }
                db.collection("user").document().set(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.inscriptionOk), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finishActivity(0)
                    } else {
                        Toast.makeText(this, getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }

    override fun onBackPressed() {

    }
}