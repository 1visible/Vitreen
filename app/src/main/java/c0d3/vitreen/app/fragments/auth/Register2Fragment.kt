package c0d3.vitreen.app.fragments.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.fragments.profile.ProfileFragment
import c0d3.vitreen.app.models.User
import c0d3.vitreen.app.utils.ChildFragment
import c0d3.vitreen.app.utils.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_register2.*

class Register2Fragment : ChildFragment() {

    private var email: String = ""
    private val db = Firebase.firestore

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString(Constants.KEYEMAIL)?.let {
            email = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register2, container, false)
    }

    // TODO: Remove this if not needed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchPro.isChecked = false
        switchPro.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                companyName.visibility = View.VISIBLE
                siret.visibility = View.VISIBLE
            } else {
                companyName.visibility = View.GONE
                siret.visibility = View.GONE
            }
        }
        submitButton.setOnClickListener {
            val user: User
            if (switchPro.isChecked) {
                user = User(
                    lastName.text.toString(),
                    firstName.text.toString(),
                    email,
                    switchPro.isChecked,
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
                    isProfessional = switchPro.isChecked,
                    phone = phoneNumber.text.toString(),
                    contactMethod = contactMethod.text.toString()
                )
            }
            db.collection("User").document().set(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.inscriptionOk),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    parentFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, ProfileFragment.newInstance())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.ErrorMessage),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(email: String): Register2Fragment = Register2Fragment().apply {
            arguments = Bundle().apply {
                putString(Constants.KEYEMAIL, email)
            }
        }
    }

}