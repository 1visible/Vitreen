package c0d3.vitreen.app.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.MainActivity
import c0d3.vitreen.app.fragments.home.HomeFragment
import c0d3.vitreen.app.utils.ChildFragment
import c0d3.vitreen.app.utils.Constants
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.*

class LoginFragment : ChildFragment() {

    private val auth = Firebase.auth
    private var user = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    // TODO: Remove this if not needed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connexionButton.setOnClickListener {
            if ((!(email.text.toString().equals(""))) && (!(password.text.toString().equals("")))) {
                if (user == null) {
                    signInUser()
                } else if (user!!.isAnonymous) {
                    removeAnonymousUser()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.emptyFields),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun removeAnonymousUser() {
        if ((user != null) && (user!!.isAnonymous)) {
            val credential = EmailAuthProvider.getCredential(
                "${UUID.randomUUID().toString()}@exemple.com",
                "xu\$dqùdlqkfgo@^`4521"
            )
            user!!.linkWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user!!.delete()
                        println("------------------------------- utilisateur effacée")
                        signInUser()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.ErrorMessage),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun signInUser() {
        Firebase.auth
            .signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(Constants.TAG, getString(R.string.SignInSucceed))
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.SignInSucceed),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    (activity as MainActivity).setBottomNavMenuIcon(R.id.navigation_home)
                    parentFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, HomeFragment.newInstance())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                } else {
                    Log.w(Constants.TAG, "Auth failed")
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.ErrorMessage),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    email.text.clear()
                    password.text.clear()
                }
            }
    }

    companion object {
        fun newInstance(): LoginFragment = LoginFragment()
    }
}