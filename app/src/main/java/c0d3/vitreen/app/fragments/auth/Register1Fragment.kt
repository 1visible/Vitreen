package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.VFragment
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.android.synthetic.main.fragment_register1.*

class Register1Fragment : VFragment(
    R.layout.fragment_register1,
    R.drawable.bigicon_authentification,
    -1
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonToRegister2.setOnClickListener {
            if (editTextEmail.text.toString().trim() != "" &&
                editTextPassword.text.toString().trim() != "" &&
                editTextPasswordConfirmation.text.toString().trim() != "") {

                if (editTextPassword.text.toString() == editTextPasswordConfirmation.text.toString()) {
                    if (user == null)
                        registerUser()
                    else if (user!!.isAnonymous)
                        linkAnonymousToCredential()
                } else {
                    editTextPassword.text.clear()
                    editTextPasswordConfirmation.text.clear()
                    showMessage(R.string.errorMessage)
                }

            } else
                showMessage(R.string.errorMessage)
        }

        buttonToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_register1_to_navigation_login)
        }

    }

    private fun registerUser() {
        auth.createUserWithEmailAndPassword(editTextEmail.text.toString(), editTextPassword.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val bundle = bundleOf("email" to editTextEmail.text.toString())
                    findNavController().navigate(R.id.action_navigation_register1_to_navigation_register2, bundle)
                } else {
                    showMessage(R.string.errorMessage)
                    // TODO: Gérer les erreurs
                    //pour l'instant ne redirige nulle part, on va juste afficher un toast disant que l'inscription a échoué
                }
            }
    }

    private fun linkAnonymousToCredential() {
        val credential = EmailAuthProvider.getCredential(editTextEmail.text.toString(), editTextPassword.text.toString())
        user!!.linkWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val bundle = bundleOf("email" to editTextEmail.text.toString())
                    findNavController().navigate(R.id.action_navigation_register1_to_navigation_register2, bundle)
                } else {
                   showMessage(R.string.errorMessage)
                    // TODO: Gérer les erreurs
                }
            }
    }

}