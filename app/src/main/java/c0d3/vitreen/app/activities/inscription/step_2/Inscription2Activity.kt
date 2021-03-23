package c0d3.vitreen.app.activities.inscription.step_2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import c0d3.vitreen.app.R
import c0d3.vitreen.app.models.Role
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class Inscription2Activity : AppCompatActivity() {

    private lateinit var roles: ArrayList<Role>

    private lateinit var textField: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscription_step_2)
        textField = findViewById<TextInputLayout>(R.id.menu)
        roles = ArrayList<Role>()
        recupRole()
    }

    private fun recupRole() {
        val db = Firebase.firestore
        db.collection("Role")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    roles.add(Role(document["name"] as String))
                }
                val items: Array<String?> = arrayOfNulls<String>(2)
                for (i in 0..1) {
                    items[i] = roles.get(i).name
                }
                val adapter = ArrayAdapter(this, R.layout.list_item, items)
                (textField.editText as? AutoCompleteTextView)?.setAdapter(adapter)
            }
    }

}