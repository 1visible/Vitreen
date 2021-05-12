package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.View
import c0d3.vitreen.app.R
import c0d3.vitreen.app.activities.observeOnce
import c0d3.vitreen.app.utils.VFragment
import kotlinx.android.synthetic.main.fragment_login.*
import java.lang.NullPointerException
import java.util.*

class LoginFragment : VFragment(
    layoutId = R.layout.fragment_login,
    topIcon = R.drawable.bigicon_authentification
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // On submit button click, try to login the user
        buttonSubmitLogin.setOnClickListener {
            // Check if required inputs are filled
            if(isAnyRequiredInputEmpty(editTextEmail, editTextPassword))
                return@setOnClickListener

            // If the user is signed out, sign in
            if (user == null)
                signIn()
            // Else if the user is signed in anonymously
            else if (!isUserSignedIn()) {
                // TODO : val credential = EmailAuthProvider.getCredential(UUID.randomUUID().toString() + FAKE_EMAIL, FAKE_PASSWORD)
                // Delete anonymous account
                try {
                    viewModel.deleteUser(user!!).observeOnce(viewLifecycleOwner, { exception ->
                        // If the call fails, show error message and hide loading spinner
                        if(handleError(exception)) return@observeOnce
                        // Else, sign in
                        signIn()
                    })
                } catch(_: NullPointerException) {
                    showMessage()
                }
            }
            // Else (the user is signed in), navigate back to home
            else
                navigateTo(R.id.from_login_to_home)
        }

        // On register button click, navigate to Register1 fragment
        buttonToRegister1.setOnClickListener {
            navigateTo(R.id.from_login_to_register1)
        }
    }

    private fun signIn() {
        val email = inputToString(editTextEmail)
        val password = inputToString(editTextPassword)

        // Check email and password after conversion
        if (email == null || password == null) {
            showMessage()
            return
        }

        viewModel.signIn(email, password).observeOnce(viewLifecycleOwner, { exception ->
            // If the call fails, show error message and hide loading spinner
            if(handleError(exception)) return@observeOnce
            // Else, redirect to home
            navigateTo(R.id.from_login_to_home)
        })
    }

}