package c0d3.vitreen.app.activities.inscription.step_2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.MainActivity
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.User
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Inscription2Activity : AppCompatActivity() {

    val TAG: String = "c0d3"

    private lateinit var email: String

    private lateinit var lastName: EditText
    private lateinit var firstName: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var contactMethod: EditText
    private lateinit var companyName: EditText
    private lateinit var siret: EditText
    private lateinit var switch: SwitchMaterial
    private lateinit var submitButton: Button


    private val KEYEMAIL = "KEYNAME"
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscription_step_2)
        if (intent != null) {
            email = intent.getStringExtra(KEYEMAIL).toString()
            lastName = findViewById<EditText>(R.id.lastName)
            firstName = findViewById<EditText>(R.id.firstName)
            phoneNumber = findViewById<EditText>(R.id.phoneNumber)
            contactMethod = findViewById<EditText>(R.id.contactMethod)
            companyName = findViewById<EditText>(R.id.companyName)
            siret = findViewById<EditText>(R.id.siret)
            switch = findViewById<SwitchMaterial>(R.id.switchPro)
            switch.isChecked = false
            switch.setOnCheckedChangeListener { buttonview, isChecked ->
                if (isChecked) {
                    companyName.visibility = View.VISIBLE
                    siret.visibility = View.VISIBLE
                } else {
                    companyName.visibility = View.GONE
                    siret.visibility = View.GONE
                }
            }
            submitButton = findViewById<Button>(R.id.submitButton)
            submitButton.setOnClickListener { it ->
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
                        Toast.makeText(this, "Inscription Terminée", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Toast.makeText(this, "Une erreur s'est produite", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }
}