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

        // On register (part 2) button click, navigate to Register2 fragment
        buttonToRegister2.setOnClickListener {
            // Check if required inputs are filled
            if(isAnyRequiredInputEmpty(editTextEmail, editTextPassword, editTextPasswordConfirmation))
                return@setOnClickListener

            val password = inputToString(editTextPassword)
            val passwordConfirmation = inputToString(editTextPasswordConfirmation)

            if(password == null || passwordConfirmation == null)
                return@setOnClickListener

            // Check if passwords are equals
            if(password != passwordConfirmation) {
                editTextPassword.editText?.text?.clear()
                editTextPasswordConfirmation.editText?.text?.clear()
                showMessage(R.string.passwords_not_equals)
                return@setOnClickListener
            }

            if (editTextPassword.editText?.text.toString() == editTextPasswordConfirmation.editText?.text.toString()) {
                if (user == null)
                    registerUser()
                else if (user!!.isAnonymous)
                    linkAnonymousToCredential()
            } else {
                editTextPassword.editText?.text?.clear()
                editTextPasswordConfirmation.editText?.text?.clear()
                showMessage(R.string.errorMessage)
            }
        }

        buttonToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_register1_to_navigation_login)
        }

    }

    private fun registerUser() {
        auth.createUserWithEmailAndPassword(editTextEmail.editText?.text.toString(), editTextPassword.editText?.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val bundle = bundleOf("email" to editTextEmail.editText?.text.toString())
                    findNavController().navigate(R.id.action_navigation_register1_to_navigation_register2, bundle)
                } else {
                    showMessage(R.string.errorMessage)
                    // TODO: Gérer les erreurs
                    //pour l'instant ne redirige nulle part, on va juste afficher un toast disant que l'inscription a échoué
                }
            }
    }

    private fun linkAnonymousToCredential() {
        val credential = EmailAuthProvider.getCredential(editTextEmail.editText?.text.toString(), editTextPassword.editText?.text.toString())
        user!!.linkWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val bundle = bundleOf("email" to editTextEmail.editText?.text.toString())
                    findNavController().navigate(R.id.action_navigation_register1_to_navigation_register2, bundle)
                } else {
                   showMessage(R.string.errorMessage)
                    // TODO: Gérer les erreurs
                }
            }
    }

}