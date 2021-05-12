package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.Constants.Companion.KEY_EMAIL
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_register1.*

class Register1Fragment : VFragment(
    layoutId = R.layout.fragment_register1,
    topIcon = R.drawable.bigicon_authentification
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // On register (part 2) button click, navigate to Register2 fragment
        buttonToRegister2.setOnClickListener {
            // Check if required inputs are filled
            if(isAnyRequiredInputEmpty(editTextEmail, editTextPassword, editTextPasswordConfirmation))
                return@setOnClickListener

            val email = inputToString(editTextEmail)
            val password = inputToString(editTextPassword)
            val passwordConfirmation = inputToString(editTextPasswordConfirmation)

            // Double check email and password after conversion
            if (email == null || password == null) {
                showMessage()
                return@setOnClickListener
            }

            // Check if passwords are equals
            if(password != passwordConfirmation) {
                editTextPassword.editText?.text?.clear()
                editTextPasswordConfirmation.editText?.text?.clear()
                showMessage(R.string.passwords_not_equals)
                return@setOnClickListener
            }

            if(user == null) {
                viewModel.registerUser(email, password).observeOnce(viewLifecycleOwner, { exception ->
                    // If the call fails, show error message and hide loading spinner
                    if(handleError(exception)) return@observeOnce
                    // Else, navigate to Register2 fragment
                    navigateTo(R.id.from_register1_to_register2, KEY_EMAIL to email)
                })
            } else if(!isUserSignedIn()) {
                try {
                    viewModel.linkUser(user!!, email, password).observeOnce(viewLifecycleOwner, { exception ->
                        // If the call fails, show error message and hide loading spinner
                        if(handleError(exception)) return@observeOnce
                        // Else, navigate to Register2 fragment
                        navigateTo(R.id.from_register1_to_register2, KEY_EMAIL to email)
                    })
                } catch (_: NullPointerException) {
                    showMessage()
                }
            } else
                navigateTo(R.id.from_register1_to_home)
        }

        // On login button click, navigate to Login fragment
        buttonToLogin.setOnClickListener {
            navigateTo(R.id.from_register1_to_login)
        }
    }

}