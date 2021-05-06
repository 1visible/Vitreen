package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.VFragment
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.*

class LoginFragment : VFragment(
    R.layout.fragment_login,
    R.drawable.bigicon_authentification,
    -1
) {

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
                showMessage(R.string.errorMessage)
        }

        buttonToRegister1.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_login_to_navigation_register1)
        }

    }

    private fun removeAnonymousUser() {
        if (user == null || !user!!.isAnonymous)
            return

        val credential = EmailAuthProvider.getCredential("${UUID.randomUUID()}@exemple.com", "xu\$dqùdlqkfgo@^`4521")
        user!!.linkWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user!!.delete()
                    user = null
                    signInUser()
                } else
                    showMessage(R.string.errorMessage)
            }
    }

    private fun signInUser() {
        auth.signInWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_navigation_login_to_navigation_home)
                } else {
                    showMessage(R.string.errorMessage)
                    editTextPassword.text.clear()
                }
            }
    }

}