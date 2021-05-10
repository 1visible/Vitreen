package c0d3.vitreen.app.fragments.auth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import c0d3.vitreen.app.R
import c0d3.vitreen.app.utils.Constants.Companion.FAKE_EMAIL
import c0d3.vitreen.app.utils.Constants.Companion.FAKE_PASSWORD
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

        // On submit button click, try to login the user
        buttonSubmitLogin.setOnClickListener {
            if(isAnyRequiredInputEmpty(editTextEmail, editTextPassword))
                return@setOnClickListener

            // If the user is signed out, sign in
            if (user == null)
                signIn()
            // Else if the user is signed in anonymously
            else if (!isUserSignedIn()) {
                // TODO : val credential = EmailAuthProvider.getCredential(UUID.randomUUID().toString() + FAKE_EMAIL, FAKE_PASSWORD)
                // Delete anonymous account
                viewModel.deleteUser(user!!).observeOnce(viewLifecycleOwner, { errorCode ->
                    // If the call fails, show error message and hide loading spinner
                    if(handleError(errorCode)) return@observeOnce
                    // Else, sign in
                    signIn()
                })
            }
            // Else (the user is signed in), navigate back to home
            else
                navigateTo(R.id.action_navigation_login_to_navigation_home)
        }

        // On register button click, navigate to Register1 fragment
        buttonToRegister1.setOnClickListener {
            navigateTo(R.id.action_navigation_login_to_navigation_register1)
        }
    }

    private fun signIn() {
        val email = inputToString(editTextEmail)
        val password = inputToString(editTextPassword)

        if(email == null || password == null)
            return

        viewModel.signIn(email, password).observeOnce(viewLifecycleOwner, { errorCode ->
            // If the call fails, show error message and hide loading spinner
            if(handleError(errorCode)) return@observeOnce
            // Else, redirect to home
            navigateTo(R.id.action_navigation_login_to_navigation_home)
        })
    }

}