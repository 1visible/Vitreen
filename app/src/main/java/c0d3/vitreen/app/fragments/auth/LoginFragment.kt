package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.fragments.home.HomeFragment
import c0d3.vitreen.app.utils.ChildFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.editTextEmail
import kotlinx.android.synthetic.main.fragment_login.editTextPassword
import kotlinx.android.synthetic.main.fragment_register1.*
import java.util.*

class LoginFragment : ChildFragment() {
    private val auth = Firebase.auth
    private var user = auth.currentUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonSubmitLogin.setOnClickListener {
            if (editTextEmail.text.toString().trim() != "" &&
                editTextPassword.text.toString().trim() != "") {

                if (user == null)
                    signInUser()
                else if (user!!.isAnonymous)
                    removeAnonymousUser()

            } else
                Toast.makeText(requireContext(), getString(R.string.emptyFields), Toast.LENGTH_SHORT).show()
        }

        buttonToRegister1.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_login_to_navigation_register1)
        }

    }

    private fun removeAnonymousUser() {
        if (user != null && user!!.isAnonymous) {
            val credential = EmailAuthProvider.getCredential("${UUID.randomUUID().toString()}@exemple.com", "xu\$dqùdlqkfgo@^`4521")

            user!!.linkWithCredential(credential)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        user!!.delete()
                        user = null
                        println("------------------------------- utilisateur effacée")
                        signInUser()
                    } else
                        Toast.makeText(requireContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()

                }

        }
    }

    private fun signInUser() {
        Firebase.auth
            .signInWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.SignInSucceed), Toast.LENGTH_SHORT).show()
                    // TODO: Remplacer par la navigation
                    (activity as MainActivity).setBottomNavMenuIcon(R.id.navigation_home)
                    parentFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, HomeFragment.newInstance())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()
                    editTextPassword.text.clear()
                }
            }
    }

}