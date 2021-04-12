package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.VFragment
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.*

class LoginFragment : VFragment(
    R.layout.fragment_login,
    R.drawable.bigicon_user,
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
                Toast.makeText(requireContext(), getString(R.string.emptyFields), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()
            }
    }

    private fun signInUser() {
        auth.signInWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.SignInSucceed), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_navigation_login_to_navigation_home)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.ErrorMessage), Toast.LENGTH_SHORT).show()
                    editTextPassword.text.clear()
                }
            }
    }

}